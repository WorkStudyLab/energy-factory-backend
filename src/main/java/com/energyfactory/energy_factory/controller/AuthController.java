package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.OAuth2SignupRequestDto;
import com.energyfactory.energy_factory.dto.OAuth2TempInfo;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
import com.energyfactory.energy_factory.dto.TokenRefreshResponseDto;
import com.energyfactory.energy_factory.exception.AuthException;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.jwt.JwtUtil;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Autowired
    public AuthController(JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 refreshToken 추출
        String refreshToken = getCookieValue(request, "refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthException(ResultCode.REFRESH_TOKEN_REQUIRED);
        }
        
        // JWT 토큰 검증 (만료 시간 등)
        if (jwtUtil.isExpired(refreshToken)) {
            throw new AuthException(ResultCode.EXPIRED_REFRESH_TOKEN);
        }
        
        // 토큰 타입 확인
        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new AuthException(ResultCode.INVALID_TOKEN_TYPE);
        }
        
        // 사용자 정보 추출 (userId, username)
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);

        // Redis에서 저장된 Refresh Token과 비교 검증
        if (!refreshTokenService.validateRefreshToken(username, refreshToken)) {
            throw new AuthException(ResultCode.INVALID_REFRESH_TOKEN);
        }

        // 새로운 Access Token 발급 (userId 포함)
        String newAccessToken = jwtUtil.createAccessToken(userId, username, "USER", 30 * 60 * 1000L);

        // 새로운 Refresh Token 발급 (userId 포함)
        String newRefreshToken = jwtUtil.createRefreshToken(userId, username, 7 * 24 * 60 * 60 * 1000L);
        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        // 쿠키에 새로운 토큰 설정
        addTokenCookie(response, "accessToken", newAccessToken, 30 * 60); // 30분
        addTokenCookie(response, "refreshToken", newRefreshToken, 7 * 24 * 60 * 60); // 7일

        return ResponseEntity.ok(ApiResponse.of(ResultCode.TOKEN_REFRESH_SUCCESS, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 refreshToken 추출
        String refreshToken = getCookieValue(request, "refreshToken");

        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                // 토큰이 유효한 경우에만 Redis에서 삭제
                if (!jwtUtil.isExpired(refreshToken)) {
                    String username = jwtUtil.getUsername(refreshToken);
                    refreshTokenService.deleteRefreshToken(username);
                }
            } catch (Exception e) {
                System.out.println("Invalid token during logout, but proceeding: " + e.getMessage());
            }
        }

        // 쿠키 삭제 (만료 시간을 0으로 설정)
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");

        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @GetMapping("/oauth-temp-info")
    @Operation(
        summary = "OAuth2 임시 정보 조회",
        description = "세션에 저장된 OAuth2 소셜 로그인 정보를 조회합니다. 프론트엔드에서 회원가입 폼에 자동 완성하기 위해 사용합니다."
    )
    public ResponseEntity<ApiResponse<OAuth2TempInfo>> getOAuth2TempInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new BusinessException(ResultCode.OAUTH_SESSION_EXPIRED);
        }

        OAuth2TempInfo tempInfo = (OAuth2TempInfo) session.getAttribute("oauth2TempInfo");

        if (tempInfo == null) {
            throw new BusinessException(ResultCode.OAUTH_SESSION_EXPIRED);
        }

        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, tempInfo));
    }

    @PostMapping("/signup-with-oauth")
    @Operation(
        summary = "OAuth2 회원가입 완료",
        description = "네이버 소셜 로그인 후 추가 정보를 입력받아 회원가입을 완료합니다. " +
                      "세션에 저장된 OAuth2 정보와 사용자가 입력한 추가 정보(생년월일, 주소)를 합쳐서 DB에 저장하고 JWT를 발급합니다."
    )
    public ResponseEntity<ApiResponse<SignupResponseDto>> signupWithOAuth2(
            @Valid @RequestBody OAuth2SignupRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 세션에서 OAuth2 임시 정보 가져오기
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ResultCode.OAUTH_SESSION_EXPIRED);
        }

        OAuth2TempInfo tempInfo = (OAuth2TempInfo) session.getAttribute("oauth2TempInfo");
        if (tempInfo == null) {
            throw new BusinessException(ResultCode.OAUTH_SESSION_EXPIRED);
        }

        log.info("OAuth2 회원가입 완료 시작: email={}, provider={}", tempInfo.getEmail(), tempInfo.getProvider());

        // 회원가입 처리
        SignupResponseDto signupResponse = userService.signupWithOAuth2(tempInfo, requestDto);

        log.info("OAuth2 회원가입 완료: userId={}", signupResponse.getId());

        // JWT 토큰 발급
        String accessToken = jwtUtil.createAccessToken(
                signupResponse.getId(),
                signupResponse.getEmail(),
                "USER",
                10 * 60 * 1000L  // 10분
        );

        String refreshToken = jwtUtil.createRefreshToken(
                signupResponse.getId(),
                signupResponse.getEmail(),
                refreshTokenExpiration * 1000L  // 7일
        );

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(signupResponse.getEmail(), refreshToken);

        // HttpOnly 쿠키에 토큰 저장
        addTokenCookie(response, "accessToken", accessToken, 10 * 60);  // 10분
        addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue());  // 7일

        // 세션 삭제 (OAuth2 임시 정보 제거)
        session.removeAttribute("oauth2TempInfo");

        log.info("JWT 토큰 발급 완료 및 세션 정리 완료");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, signupResponse));
    }

    @GetMapping("/link/naver")
    @Operation(
        summary = "네이버 계정 연동 시작",
        description = "로그인된 LOCAL 사용자가 네이버 계정을 연동하기 위해 OAuth2 플로우를 시작합니다. " +
                      "세션에 연동 모드를 저장한 후 네이버 로그인 페이지로 리다이렉트합니다."
    )
    public void linkNaverAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if (userDetails == null) {
            log.error("User not authenticated");
            response.sendRedirect("/api/auth/login?error=not_authenticated");
            return;
        }

        Long userId = userDetails.getUser().getId();

        log.info("========== Start Naver Account Link ==========");
        log.info("User ID: {}", userId);

        // 세션에 연동 모드 저장
        HttpSession session = request.getSession();
        session.setAttribute("linkMode", true);
        session.setAttribute("linkUserId", userId);
        session.setMaxInactiveInterval(10 * 60); // 10분 유효

        log.info("Link mode saved to session");
        log.info("Redirecting to: /oauth2/authorization/naver");
        log.info("==============================================");

        // 네이버 OAuth2 인증으로 리다이렉트
        response.sendRedirect("/oauth2/authorization/naver");
    }

    /**
     * 쿠키에서 값을 추출하는 헬퍼 메서드
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * HttpOnly 쿠키에 토큰 추가
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

    /**
     * 쿠키 삭제 (만료 시간을 0으로 설정)
     */
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 만료
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }
}
