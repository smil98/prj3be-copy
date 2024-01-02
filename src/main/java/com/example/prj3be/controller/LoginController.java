package com.example.prj3be.controller;

import com.example.prj3be.dto.LoginDto;
import com.example.prj3be.dto.MemberAuthDto;
import com.example.prj3be.dto.TokenDto;
import com.example.prj3be.jwt.TokenProvider;
import com.example.prj3be.service.oauth.OauthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final OauthService oauthService;


    //    @Cacheable(value = "accesstokenCache",cacheManager = "accessTokenCacheManager",key = "#accessToken")
    @GetMapping("/accessToken")
    public MemberAuthDto isTokenValid(@RequestHeader("Authorization")String accessToken){
        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }
        if(tokenProvider.validateToken(accessToken)){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            MemberAuthDto dto = new MemberAuthDto(authentication.getName(),authentication.getAuthorities().stream().toList().get(0).toString());
//            dto.setLogId(authentication.getName());
//            dto.setRole(authentication.getAuthorities().stream().toList().get(0).toString());
            return dto;
        }
        // 유효하지 않은 토큰이거나 다른 이유로 인증에 실패한 경우
        // 캐시에 저장하지 않도록 null을 반환하거나 다른 방식으로 처리할 수 있습니다.
        return null;
    }
    @GetMapping("/refreshToken")
//    @CacheEvict(value = "accesstokenCache", key = "#accessToken")
    public ResponseEntity<TokenDto> byRefreshToken(@RequestHeader("Authorization")String refreshToken){
        System.out.println("LoginController.byRefreshToken's refreshToken = " + refreshToken);
        if(StringUtils.hasText(refreshToken) && refreshToken.startsWith("Bearer ")){
            refreshToken = refreshToken.substring(7);
        }
        Authentication authentication = tokenProvider.updateTokensByRefreshToken(refreshToken);

        System.out.println("LoginController.byRefreshToken's authentication = " + authentication);
        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenDto tokens = tokenProvider.createTokens(authentication);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto){
        System.out.println("loginDto.getEmail() = " + loginDto.getEmail());
        System.out.println("loginDto.getPassword() = " + loginDto.getPassword());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        System.out.println("LoginController.authorize");
        System.out.println("authenticationToken = " + authenticationToken);

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("authentication = " + authentication);

            TokenDto tokens = tokenProvider.createTokens(authentication);

            System.out.println("tokens = " + tokens);

            return new ResponseEntity<>(new TokenDto(tokens.getAccessToken(), tokens.getRefreshToken()), HttpStatus.OK);
        } catch (AuthenticationException e){
            System.out.println("인증 실패 :"+e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/api/logout")
    public ResponseEntity logout(@RequestHeader("Authorization")String refreshToken){
        if(StringUtils.hasText(refreshToken) && refreshToken.startsWith("Bearer ")){
            refreshToken = refreshToken.substring(7);
        }

        if(refreshToken != null){
            Long id = tokenProvider.deleteRefreshToken(refreshToken);
            if(id != null) {
                return oauthService.logoutRequest(id);
            }
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
