package com.energyfactory.energy_factory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {


    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Autowired
    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Refresh Token을 Redis에 저장
     * @param username 사용자명
     * @param refreshToken 리프레시 토큰
     */
    public void saveRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.SECONDS);
    }

    /**
     * Refresh Token 조회
     * @param username 사용자명
     * @return 저장된 리프레시 토큰
     */
    public String getRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token 삭제
     * @param username 사용자명
     */
    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
    }

    /**
     * Refresh Token 유효성 검증
     * @param username 사용자명
     * @param refreshToken 검증할 리프레시 토큰
     * @return 유효성 여부
     */
    public boolean validateRefreshToken(String username, String refreshToken) {
        String storedToken = getRefreshToken(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    /**
     * Refresh Token 존재 여부 확인
     * @param username 사용자명
     * @return 존재 여부
     */
    public boolean existsRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
