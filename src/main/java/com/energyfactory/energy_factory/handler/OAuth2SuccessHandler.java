package com.energyfactory.energy_factory.handler;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.OAuth2TempInfo;
import com.energyfactory.energy_factory.jwt.JwtUtil;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.Provider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Value("${app.oauth2.signup-url}")
    private String signupUrl;

    @Value("${app.oauth2.home-url}")
    private String homeUrl;

    @Value("${app.oauth2.mypage-url}")
    private String mypageUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        HttpSession session = request.getSession(false);

        // 1. 계정 연동 모드인지 확인
        if (session != null && Boolean.TRUE.equals(session.getAttribute("linkMode"))) {
            handleAccountLink(request, response, session, customUserDetails);
            return;
        }

        Long userId = customUserDetails.getUser().getId();

        // 2. 신규 사용자인지 기존 사용자인지 구분
        // userId가 null이면 DB에 저장되지 않은 신규 사용자
        if (userId == null) {
            // 신규 사용자: 세션에 OAuth2 정보 저장 후 signup으로 리다이렉트
            handleNewUser(request, response, customUserDetails);
        } else {
            // 기존 사용자: JWT 발급 후 홈으로 리다이렉트
            handleExistingUser(request, response, customUserDetails);
        }
    }

    /**
     * 신규 사용자 처리: 세션에 OAuth2 정보 저장 후 signup 페이지로 리다이렉트
     */
    private void handleNewUser(HttpServletRequest request, HttpServletResponse response,
                                CustomUserDetails customUserDetails) throws IOException {
        log.info("========== New OAuth2 User ==========");
        log.info("Email: {}", customUserDetails.getUser().getEmail());
        log.info("Name: {}", customUserDetails.getUser().getName());
        log.info("Provider: {}", customUserDetails.getUser().getProvider());

        // OAuth2 정보를 세션에 저장
        HttpSession session = request.getSession();
        OAuth2TempInfo tempInfo = OAuth2TempInfo.builder()
                .provider(customUserDetails.getUser().getProvider().name())
                .providerId(customUserDetails.getUser().getProviderId())
                .email(customUserDetails.getUser().getEmail())
                .name(customUserDetails.getUser().getName())
                .phoneNumber(customUserDetails.getUser().getPhoneNumber())
                .build();

        session.setAttribute("oauth2TempInfo", tempInfo);
        session.setMaxInactiveInterval(30 * 60); // 30분 유효

        log.info("OAuth2 temp info saved to session");
        log.info("Redirecting to: {}", signupUrl + "?oauth=pending");
        log.info("=====================================");

        // signup 페이지로 리다이렉트 (oauth=pending 파라미터 추가)
        getRedirectStrategy().sendRedirect(request, response, signupUrl + "?oauth=pending");
    }

    /**
     * 기존 사용자 처리: JWT 발급 후 홈으로 리다이렉트
     */
    private void handleExistingUser(HttpServletRequest request, HttpServletResponse response,
                                     CustomUserDetails customUserDetails) throws IOException {
        Long userId = customUserDetails.getUser().getId();
        String username = customUserDetails.getUsername();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        log.info("========== Existing OAuth2 User ==========");
        log.info("User ID: {}", userId);
        log.info("Email: {}", username);
        log.info("=========================================");

        // Access Token 생성 (userId 포함, 10분)
        String accessToken = jwtUtil.createAccessToken(userId, username, role, 10 * 60 * 1000L);

        // Refresh Token 생성 (userId 포함, 7일)
        String refreshToken = jwtUtil.createRefreshToken(userId, username, refreshTokenExpiration * 1000L);

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(username, refreshToken);

        // HttpOnly 쿠키에 토큰 저장
        addTokenCookie(response, "accessToken", accessToken, 10 * 60); // 10분
        addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue()); // 7일

        // 홈으로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, homeUrl);
    }

    /**
     * 계정 연동 처리: 로그인된 LOCAL 사용자가 네이버 계정을 연동
     */
    private void handleAccountLink(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, CustomUserDetails customUserDetails) throws IOException {
        Long linkUserId = (Long) session.getAttribute("linkUserId");

        if (linkUserId == null) {
            log.error("linkUserId not found in session");
            getRedirectStrategy().sendRedirect(request, response, homeUrl + "?error=session_expired");
            return;
        }

        String providerId = customUserDetails.getUser().getProviderId();
        Provider provider = customUserDetails.getUser().getProvider();

        log.info("========== Account Link ==========");
        log.info("User ID: {}", linkUserId);
        log.info("Provider: {}", provider);
        log.info("Provider ID: {}", providerId);

        try {
            // 계정 연동 실행
            userService.linkNaverAccount(linkUserId, providerId, provider);

            // 세션 정리
            session.removeAttribute("linkMode");
            session.removeAttribute("linkUserId");

            log.info("Account link successful");
            log.info("Redirecting to: {}", mypageUrl + "?link=success");
            log.info("==================================");

            // 마이페이지로 리다이렉트 (연동 성공 메시지)
            getRedirectStrategy().sendRedirect(request, response, mypageUrl + "?link=success");
        } catch (Exception e) {
            log.error("Account link failed: {}", e.getMessage());
            log.error("==================================");

            // 세션 정리
            session.removeAttribute("linkMode");
            session.removeAttribute("linkUserId");

            // 에러 메시지와 함께 마이페이지로 리다이렉트
            String errorMessage = e.getMessage().contains("이미 소셜 계정이") ? "already_linked" :
                                  e.getMessage().contains("이미 다른 사용자가") ? "already_in_use" : "unknown_error";
            getRedirectStrategy().sendRedirect(request, response, mypageUrl + "?link=error&reason=" + errorMessage);
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
