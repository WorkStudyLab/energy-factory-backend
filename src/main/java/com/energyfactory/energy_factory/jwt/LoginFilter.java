package com.energyfactory.energy_factory.jwt;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.LoginRequestDto;
import com.energyfactory.energy_factory.dto.LoginResponseDto;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            System.out.println("=== Login Attempt Started ===");

            // JSON만 처리 (form-data 지원 제거)
            String contentType = request.getContentType();
            System.out.println("Content-Type: " + contentType);

            if (contentType == null || !contentType.contains("application/json")) {
                System.out.println("ERROR: Invalid Content-Type");
                throw new AuthenticationException("Content-Type must be application/json") {
                    @Override
                    public String getMessage() {
                        return super.getMessage();
                    }
                };
            }

            // JSON 요청 파싱
            LoginRequestDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            System.out.println("Email: " + email);
            System.out.println("Password length: " + (password != null ? password.length() : 0));

            // 이메일과 비밀번호 검증
            if (email == null || email.trim().isEmpty()) {
                System.out.println("ERROR: Email is empty");
                throw new AuthenticationException("Email is required") {
                    @Override
                    public String getMessage() {
                        return super.getMessage();
                    }
                };
            }

            if (password == null || password.trim().isEmpty()) {
                System.out.println("ERROR: Password is empty");
                throw new AuthenticationException("Password is required") {
                    @Override
                    public String getMessage() {
                        return super.getMessage();
                    }
                };
            }

            // Spring Security에서 username으로 처리하지만 실제로는 email
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

            System.out.println("Attempting authentication with AuthenticationManager...");

            // AuthenticationManager로 인증 처리
            Authentication result = authenticationManager.authenticate(authToken);

            System.out.println("Authentication successful: " + result.isAuthenticated());
            return result;

        } catch (IOException e) {
            System.out.println("ERROR: Failed to parse request - " + e.getMessage());
            throw new AuthenticationException("Failed to parse authentication request") {
                @Override
                public String getMessage() {
                    return super.getMessage() + ": " + e.getMessage();
                }
            };
        } catch (AuthenticationException e) {
            System.out.println("ERROR: Authentication failed - " + e.getMessage());
            throw e;
        }
    }

    //로그인 성공시 실행하는 메서드(jwt 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        try {
            System.out.println("=== Login Success - Generating Tokens ===");

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = customUserDetails.getUsername();
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            System.out.println("Username: " + username);
            System.out.println("Role: " + role);

            // 토큰 생성
            String accessToken = jwtUtil.createAccessToken(username, role, 30 * 60 * 1000L);
            String refreshToken = jwtUtil.createRefreshToken(username, 7 * 24 * 60 * 60 * 1000L);

            System.out.println("Access Token generated: " + (accessToken != null));
            System.out.println("Refresh Token generated: " + (refreshToken != null));

            // Redis에 Refresh Token 저장
            refreshTokenService.saveRefreshToken(username, refreshToken);
            System.out.println("Refresh Token saved to Redis");

            // HttpOnly 쿠키에 토큰 저장 (보안 강화)
            addTokenCookie(response, "accessToken", accessToken, 30 * 60); // 30분
            addTokenCookie(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60); // 7일
            System.out.println("Tokens added to cookies");

            // 응답 바디에도 토큰 포함 (개발 편의성 - Swagger 등에서 확인 가능)
            LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();

            writeJsonResponse(response, ApiResponse.of(ResultCode.LOGIN_SUCCESS, loginResponse));
            System.out.println("Login response sent successfully");

        } catch (Exception e) {
            System.out.println("ERROR in successfulAuthentication: " + e.getMessage());
            e.printStackTrace();
            unsuccessfulAuthentication(request, response, null);
        }
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        try {
            System.out.println("=== Login Failed ===");
            if (failed != null) {
                System.out.println("Failure reason: " + failed.getMessage());
                failed.printStackTrace();
            }
            writeJsonResponse(response, ApiResponse.of(ResultCode.LOGIN_FAILED, null));
        } catch (IOException e) {
            System.out.println("ERROR writing failure response: " + e.getMessage());
            response.setStatus(500);
        }
    }

    private void writeJsonResponse(HttpServletResponse response, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (apiResponse.getStatus() != 200) {
            response.setStatus(apiResponse.getStatus());
        }

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
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
