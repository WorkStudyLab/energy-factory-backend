package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.TokenRefreshResponseDto;
import com.energyfactory.energy_factory.exception.AuthException;
import com.energyfactory.energy_factory.jwt.JwtUtil;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final JwtUtil jwtUtil;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refreshToken(@RequestBody Map<String, String> request) {
        
        String refreshToken = request.get("refreshToken");
        
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
        
        // 사용자명 추출
        String username = jwtUtil.getUsername(refreshToken);
        
        // Redis에서 저장된 Refresh Token과 비교 검증
        if (!refreshTokenService.validateRefreshToken(username, refreshToken)) {
            throw new AuthException(ResultCode.INVALID_REFRESH_TOKEN);
        }
        
        // 새로운 Access Token 발급
        String newAccessToken = jwtUtil.createAccessToken(username, "USER", 30 * 60 * 1000L);
        
        // 새로운 Refresh Token 발급
        String newRefreshToken = jwtUtil.createRefreshToken(username, 7 * 24 * 60 * 60 * 1000L);
        refreshTokenService.saveRefreshToken(username, newRefreshToken);
        
        TokenRefreshResponseDto responseData = TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
        return ResponseEntity.ok(ApiResponse.of(ResultCode.TOKEN_REFRESH_SUCCESS, responseData));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
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
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}
