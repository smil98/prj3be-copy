package com.example.prj3be.service.oauth;

import com.example.prj3be.constant.Role;
import com.example.prj3be.constant.SocialLoginType;
import com.example.prj3be.domain.QSocialToken;
import com.example.prj3be.repository.SocialTokenRepository;
import com.example.prj3be.domain.SocialToken;
import com.example.prj3be.domain.Member;
import com.example.prj3be.dto.SocialUser;
import com.example.prj3be.dto.SocialOauthToken;
import com.example.prj3be.dto.TokenDto;
import com.example.prj3be.exception.OAuthException;
import com.example.prj3be.jwt.JwtFilter;
import com.example.prj3be.jwt.TokenProvider;
import com.example.prj3be.repository.MemberRepository;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class OauthService {
    private final List<SocialOauth> socialOauthList;
    private final List<SocialTokenManager> socialTokenManagers;
    private final MemberRepository memberRepository;
    private final SocialTokenRepository socialTokenRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    @Autowired
    private EntityManager entityManager;

    // 소셜 타입 찾기 : 로그인용
    private SocialOauth findSocialOauthByType(SocialLoginType socialLoginType) {
        return socialOauthList.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new OAuthException("알 수 없는 SocialLoginType 입니다."));
    }

    // 소셜 타입 찾기 : 토큰 관리용

    private SocialTokenManager findSocialTokenManagerByType(SocialLoginType socialLoginType) {
        return socialTokenManagers.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new OAuthException("알 수 없는 SocialLoginType 입니다."));
    }

    // 로그인
    public String loginRequest(SocialLoginType socialLoginType) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        return socialOauth.getOauthRedirectURL();
    }


    @Transactional
    public ResponseEntity<TokenDto> oAuthLogin(SocialLoginType socialLoginType, String code) throws IOException {
        SocialOauth socialOauth = findSocialOauthByType(socialLoginType);
        ResponseEntity<String> accessTokenResponse = socialOauth.requestAccessToken(code);
        SocialOauthToken oAuthToken = socialOauth.getAccessToken(accessTokenResponse);
        ResponseEntity<String> userInfoResponse = socialOauth.requestUserInfo(oAuthToken);
        SocialUser socialUser = socialOauth.getUserInfo(userInfoResponse);
        System.out.println("socialUser = " + socialUser);

        String name = socialUser.getName();
        String email = socialUser.getEmail();

        //DB에 해당 유저가 없는지 조회
        Optional<Member> existingMember = memberRepository.findByEmail(email);

        //정보 받을 객체 선언, 생성 + 초기화는 하지 않음
        Member member;
        SocialToken tokenInfo;

        if(existingMember.isPresent()) {
            member = existingMember.get();
        } else {
            member = new Member();
            member.setEmail(email);
            member.setNickName(name);
            member.setPassword(encoder.encode(email));
            // user로 role 지정
            member.setRole(Role.ROLE_SOCIAL);
            memberRepository.save(member);
            System.out.println("New member created : " + member);
        }

        //SocialToken에 소셜 타입(로그아웃/토큰 갱신 시 분류 위해), access_token, refresh_token, expires_in 저장
        tokenInfo = new SocialToken();
        tokenInfo.setId(member.getId());
        tokenInfo.setSocialLoginType(socialLoginType);
        tokenInfo.setAccessToken(oAuthToken.getAccess_token());
        tokenInfo.setExpiresIn(oAuthToken.getExpires_in());
        tokenInfo.setRefreshToken(oAuthToken.getRefresh_token());
        tokenInfo.setRefreshTokenExpiresIn(oAuthToken.getRefresh_token_expires_in());
        tokenInfo.setTokenType(oAuthToken.getToken_type());
        socialTokenRepository.save(tokenInfo);
        System.out.println("New token created : " + tokenInfo);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(member.getEmail(), member.getEmail());
        System.out.println("authenticationToken = " + authenticationToken);

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("authentication = " + authentication);

            TokenDto tokens = tokenProvider.createTokens(authentication);

            System.out.println("jwt = " + tokens);

            //httpHeader에 jwtToken 저장
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + tokens);
            System.out.println("httpHeaders = " + httpHeaders);

            return new ResponseEntity<>(new TokenDto(tokens.getAccessToken(), tokens.getRefreshToken()), HttpStatus.OK);
        } catch (AuthenticationException e){
            System.out.println("인증 실패 :"+e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity logoutRequest(Long id) {
        System.out.println("소셜 logoutRequest 연결 성공");
        SocialLoginType socialLoginType = socialTokenRepository.findSocialLoginTypeById(id);
        System.out.println("socialLoginType = " + socialLoginType);
        SocialTokenManager socialTokenManager = this.findSocialTokenManagerByType(socialLoginType);
        System.out.println("socialTokenManager = " + socialTokenManager);

        ResponseEntity response = socialTokenManager.socialLogout(id);
        System.out.println("소셜 logoutRequest 끝 지운: " + response);

        return ResponseEntity.ok(response.getBody());
    }

    // 토큰 갱신
    @Transactional
    public ResponseEntity<Integer> refreshAccessToken(String refreshToken) {
        System.out.println("OauthService.refreshAccessToken");
        //리프래쉬 토큰을 이용해 사용자의 아이디를 가져옴
        Long id = tokenProvider.getIdRefreshToken(refreshToken);
        System.out.println("id = " + id);

        try{
            //아이디로 소셜타입을 찾아와 저장
            //소셜 타입에 따른 메소드로 연결
            SocialLoginType socialLoginType = socialTokenRepository.findSocialLoginTypeById(id);
            SocialTokenManager socialTokenManager = this.findSocialTokenManagerByType(socialLoginType);
            System.out.println("토큰 유효한지 확인하고 요청 보내기");
            //갱신이 필요한지 확인
            boolean refreshNeeded = isRefreshNeeded(id);
            if(refreshNeeded) {
                //요청 보내기
                ResponseEntity<String> response = socialTokenManager.checkAndRefreshToken(id);
                System.out.println("요청 보내고 받은 응답 " + response);
                System.out.println("받은 거 프로세스");
                Map<String, Object> tokenInfoMap = processRefreshResponse(response);
                //받은 거 프로세스해서
                System.out.println("tokenInfoMap = " + tokenInfoMap);
                System.out.println("테이블 업데이트");
                //테이블 업데이트하고
                updateTokenInfo(id, tokenInfoMap);
                //expires_in 리턴하기
                System.out.println("expires_in 리턴 중");
                Object expiresInObject = tokenInfoMap.get("expiresIn");
                System.out.println("expiresInObject = " + expiresInObject);
                System.out.println("expiresInObject.getClass() = " + expiresInObject.getClass());
                Integer expiresIn = Integer.parseInt(expiresInObject.toString());
                return ResponseEntity.ok().body(expiresIn);
            } else {
                return ResponseEntity.ok().body(3600); // 갱신이 불필요하기 때문에 주기 다시 세팅
            }
        } catch (OAuthException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (HttpClientErrorException.Unauthorized e) {
            System.out.println("소셜 토큰이 유효하지 않음");
            tokenProvider.deleteRefreshToken(refreshToken);
            //소셜 토큰이 유효하지 않으면 Unauthorized 리턴됨
            System.out.println("e.getMessage() = " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RestClientException e) {
            System.out.println("뭔가 더 큰 에러 남");
            System.out.println("e.getMessage() = " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    public boolean isRefreshNeeded(Long id) {
        System.out.println("OauthService.isRefreshNeeded");
        System.out.println("id = " + id);

        //현재 시간 추출
        LocalDateTime currentTime = LocalDateTime.now();
        System.out.println("currentTime = " + currentTime);

        //테이블에서 리프래쉬 토큰의 만료 시간을 가져옴
        Integer refreshTokenExpiresIn = socialTokenRepository.getRefreshTokenExpireTimeById(id);
        System.out.println("refreshTokenExpiresIn = " + refreshTokenExpiresIn);

        // 테이블에서 액세스 토큰의 만료 시간을 가져옴
        Integer expiresIn = socialTokenRepository.getExpireTimeById(id);
        System.out.println("expiresIn = " + expiresIn);

        // 최근 업데이트 된 시간을 가져옴
        LocalDateTime updateTime = socialTokenRepository.getUpdateTimeById(id);
        System.out.println("updateTimeTest = " + updateTime);

        // 리프래쉬 토큰이 만료됐는지 확인
        // 만료 시간 계산 -> 최근 업데이트 된 시간 + 토큰 만료 시간 (초)
        // 현재 시간 vs 업데이트 시간 + 토큰 만료 시간
        LocalDateTime refreshExpireTime = updateTime.plusSeconds(refreshTokenExpiresIn);
        System.out.println("refreshExpireTime = " + refreshExpireTime);
        boolean isRefreshTokenExpired = currentTime.isAfter(refreshExpireTime);
        System.out.println("isRefreshTokenExpired = " + isRefreshTokenExpired);

        // 액세스 토큰이 만료 됐는지 확인
        LocalDateTime expireTime = updateTime.plusSeconds(expiresIn);
        System.out.println("expireTime = " + expireTime);
        boolean isAccessTokenExpired = currentTime.isAfter(expireTime);
        System.out.println("토큰이 만료됐는지: " + isAccessTokenExpired);

        // 리프래쉬 토큰이 만료됨 -> 재로그인 요청 -> UNAUTHORIZED
        // 리프래쉬 토큰이 유효하고 액세스 토큰 또한 유효 -> 갱신 불필요
        // 리프래쉬 토큰이 유효하고 액세스 토큰 만료 -> 갱신 가능
        if(isRefreshTokenExpired) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        } else {
            if(isAccessTokenExpired) {
                System.out.println("리프래쉬 토큰이 유효하고 액세스 토큰이 만료됨: 갱신 필요");
                return true;
            }
            System.out.println("리프래쉬 토큰이 유효하고 액세스 토큰 또한 유효함");
            return false;
        }
    };

    public Map<String, Object> processRefreshResponse(ResponseEntity<String> response) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(Objects.requireNonNull(response.getBody()));
        Map<String, Object> tokenInfoMap = new HashMap<>();

        tokenInfoMap.put("accessToken", jsonObject.get("access_token"));
        tokenInfoMap.put("expiresIn", jsonObject.get("expires_in"));

        //토큰 갱신 요청 시 refreshToken이 만료되지 않았을 경우 access_token, expires_in만 돌려줌
        //따라서 nullPointException 발생 가능, 미리 응답에 있는지 체크하고 있으면 넣고 없으면 null 처리하여 수행
        if (jsonObject.containsKey("refresh_token")) {
            tokenInfoMap.put("refreshToken", jsonObject.get("refresh_token"));
            tokenInfoMap.put("refreshTokenExpiresIn", jsonObject.get("refresh_token_expires_in"));
        } else {
            tokenInfoMap.put("refreshToken", null);
        }

        return tokenInfoMap;
    };

    @Transactional
    public void updateTokenInfo(Long id, Map<String, Object> tokenInfoMap) {
        System.out.println("OauthService.updateTokenInfo");
        //따라서 Querydsl with Spring Data JPA로 있을 때만 업데이트하고 없을 때는 업데이트 하지 않도록 함
        QSocialToken socialToken = QSocialToken.socialToken;

        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, socialToken);
        updateClause
                .set(socialToken.accessToken, tokenInfoMap.get("accessToken").toString())
                .set(socialToken.expiresIn, Integer.parseInt(tokenInfoMap.get("expiresIn").toString()));

        if (tokenInfoMap.get("refreshToken") != null && tokenInfoMap.get("refreshTokenExpiresIn") != null) {
            updateClause
                    .set(socialToken.refreshToken, tokenInfoMap.get("refreshToken").toString())
                    .set(socialToken.refreshTokenExpiresIn, Integer.parseInt(tokenInfoMap.get("refreshTokenExpiresIn").toString()));
        } else {
            //refreshToken이 null일 경우 기존 데이터 보존
            updateClause
                    .setNull(socialToken.refreshToken)
                    .setNull(socialToken.refreshTokenExpiresIn);
        }
        updateClause.where(socialToken.id.eq(id)).execute();
    };

    public ResponseEntity deleteSocial(Long id) {
        // (레코드 전체, socialTokenRepository -> findAndDeleteTokenById 사용)
        SocialLoginType socialLoginType = socialTokenRepository.findSocialLoginTypeById(id);
        SocialTokenManager socialTokenManager = this.findSocialTokenManagerByType(socialLoginType);
        ResponseEntity response = socialTokenManager.revokeToken(id);

        return response;
    }

}
