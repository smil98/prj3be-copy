package com.example.prj3be.controller;

import com.example.prj3be.constant.SocialLoginType;
import com.example.prj3be.dto.TokenDto;
import com.example.prj3be.exception.OAuthException;
import com.example.prj3be.jwt.TokenProvider;
import com.example.prj3be.service.oauth.OauthService;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sqm.sql.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OauthController {
    private final OauthService oauthService;

    @GetMapping("/{socialLoginType}") //MemberLogin.js > handleSocialLogin
    public String socialLoginType(@PathVariable(name="socialLoginType") String socialLoginType) {
        SocialLoginType type = SocialLoginType.valueOf(socialLoginType);
        String redirectUrl = oauthService.loginRequest(type);

        return redirectUrl;
    }

    @GetMapping("/{socialLoginType}/callback")
    public ResponseEntity<TokenDto> oAuthLogin(@PathVariable(name="socialLoginType") String socialLoginType,
                                                  @RequestParam(name="code") String code) throws IOException {
        SocialLoginType type = SocialLoginType.valueOf(socialLoginType.toUpperCase());
        ResponseEntity<TokenDto> tokenDto = oauthService.oAuthLogin(type, code);
        return tokenDto;
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<Integer> oAuthRefresh(@RequestHeader("Authorization")String refreshToken) {
        System.out.println("OauthController.oAuthRefresh");
        if(StringUtils.hasText(refreshToken) && refreshToken.startsWith("Bearer ")){
            refreshToken = refreshToken.substring(7);
        }
        ResponseEntity<Integer> expiresIn = oauthService.refreshAccessToken(refreshToken);

        return expiresIn;
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<String> oAuthExceptionHandler(OAuthException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConversionException.class)
    public ResponseEntity<String> conversionExceptionHandler(ConversionException e) {
        return new ResponseEntity<>("지원되지 않는 SocialLoginType입니다.", HttpStatus.NOT_FOUND);
    }
}
