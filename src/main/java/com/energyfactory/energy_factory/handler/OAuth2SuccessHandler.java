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

    @Value("${app.oauth2.redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        // Access Token 생성 (10분)
        String accessToken = jwtUtil.createAccessToken(username, role, 10 * 60 * 1000L);

        // Refresh Token 생성 (7일, application.yml의 설정 사용)
        String refreshToken = jwtUtil.createRefreshToken(username, refreshTokenExpiration * 1000L);

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(username, refreshToken);

        // HttpOnly 쿠키에 토큰 저장 (보안 강화)
        addTokenCookie(response, "accessToken", accessToken, 10 * 60); // 10분
        addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue()); // 7일

        // 프론트엔드로 리다이렉트 (토큰은 쿠키에 있음)
        getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
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
        cookie.setSecure(false);   // 개발 환경: false, 운영 환경: true (HTTPS)
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초)
        // cookie.setSameSite("Lax"); // CSRF 방어 (Spring Boot 3.x에서는 별도 설정 필요)

        response.addCookie(cookie);
    }
}
