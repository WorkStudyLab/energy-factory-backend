package com.energyfactory.energy_factory.jwt;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.LoginRequestDto;
import com.energyfactory.energy_factory.dto.LoginResponseDto;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
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
            // JSON만 처리 (form-data 지원 제거)
            String contentType = request.getContentType();
            
            if (contentType == null || !contentType.contains("application/json")) {
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
            
            // 이메일과 비밀번호 검증
            if (email == null || email.trim().isEmpty()) {
                throw new AuthenticationException("Email is required") {
                    @Override
                    public String getMessage() {
                        return super.getMessage();
                    }
                };
            }
            
            if (password == null || password.trim().isEmpty()) {
                throw new AuthenticationException("Password is required") {
                    @Override
                    public String getMessage() {
                        return super.getMessage();
                    }
                };
            }
            
            // Spring Security에서 username으로 처리하지만 실제로는 email
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
            
            // AuthenticationManager로 인증 처리
            return authenticationManager.authenticate(authToken);
            
        } catch (IOException e) {
            throw new AuthenticationException("Failed to parse authentication request") {
                @Override
                public String getMessage() {
                    return super.getMessage() + ": " + e.getMessage();
                }
            };
        }
    }

    //로그인 성공시 실행하는 메서드(jwt 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        try {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = customUserDetails.getUsername();
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            // 토큰 생성
            String accessToken = jwtUtil.createAccessToken(username, role, 30 * 60 * 1000L);
            String refreshToken = jwtUtil.createRefreshToken(username, 7 * 24 * 60 * 60 * 1000L);

            // Redis에 Refresh Token 저장
            refreshTokenService.saveRefreshToken(username, refreshToken);

            // 응답 생성 (클라이언트에게는 토큰 ID만 전달하거나, 실제 토큰 전달)
            LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken) // 실제 토큰을 전달 (또는 토큰 ID만 전달할 수도 있음)
                    .tokenType("Bearer")
                    .build();

            writeJsonResponse(response, ApiResponse.of(ResultCode.LOGIN_SUCCESS, loginResponse));
            
        } catch (Exception e) {
            unsuccessfulAuthentication(request, response, null);
        }
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        try {
            writeJsonResponse(response, ApiResponse.of(ResultCode.LOGIN_FAILED, null));
        } catch (IOException e) {
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
}
