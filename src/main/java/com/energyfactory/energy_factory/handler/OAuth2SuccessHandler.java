package com.energyfactory.energy_factory.handler;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.jwt.JwtUtil;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Value("${app.oauth2.signup-url}")
    private String signupUrl;

    @Value("${app.oauth2.home-url}")
    private String homeUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUser().getId();
        String username = customUserDetails.getUsername();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        // Access Token 생성 (userId 포함, 10분)
        String accessToken = jwtUtil.createAccessToken(userId, username, role, 10 * 60 * 1000L);

        // Refresh Token 생성 (userId 포함, 7일, application.yml의 설정 사용)
        String refreshToken = jwtUtil.createRefreshToken(userId, username, refreshTokenExpiration * 1000L);

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(username, refreshToken);

        // HttpOnly 쿠키에 토큰 저장 (보안 강화)
        addTokenCookie(response, "accessToken", accessToken, 10 * 60); // 10분
        addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue()); // 7일

        // 사용자 정보 완성도에 따라 리다이렉트 URL 결정
        String redirectUrl = determineRedirectUrl(customUserDetails);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    /**
     * 사용자 정보 완성도를 확인하여 리다이렉트 URL 결정
     * - 필수 정보가 없으면: 회원가입 화면 (추가 정보 입력)
     * - 모든 정보가 있으면: 홈 화면
     */
    private String determineRedirectUrl(CustomUserDetails customUserDetails) {
        // 생년월일과 주소가 모두 있으면 기존 회원으로 간주
        boolean hasBirthDate = customUserDetails.getUser().getBirthDate() != null;
        boolean hasAddress = customUserDetails.getUser().getAddress() != null
                          && !customUserDetails.getUser().getAddress().trim().isEmpty();

        if (hasBirthDate && hasAddress) {
            // 기존 회원: 홈으로 이동
            return homeUrl;
        } else {
            // 신규 회원 또는 정보 미완성: 회원가입 화면으로 이동
            return signupUrl;
        }
    }

    /**
     * HttpOnly 쿠키에 토큰 추가
     *
     * @param response HTTP 응답
     * @param name     쿠키 이름
     * @param value    토큰 값
     * @param maxAge   만료 시간 (초)
     */
    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // JavaScript 접근 방지 (XSS 방어)
        cookie.setSecure(true);    // HTTPS에서만 쿠키 전송 (프로덕션 환경)
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초)
        cookie.setAttribute("SameSite", "None");  // Cross-site 쿠키 허용 (Secure=true 필요)

        response.addCookie(cookie);
    }
}
