package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 인증 코드 및 리셋 토큰 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFY_CODE_PREFIX = "verify_code:";
    private static final String RESET_TOKEN_PREFIX = "reset_token:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRATION = 5; // 5분
    private static final long TOKEN_EXPIRATION = 10; // 10분
    private static final long RATE_LIMIT_DURATION = 1; // 1분

    private final SecureRandom random = new SecureRandom();

    /**
     * 6자리 랜덤 인증 코드 생성
     */
    public String generateCode() {
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }

    /**
     * 인증 코드를 Redis에 저장
     * @param email 이메일 주소
     * @param code 인증 코드
     */
    public void saveCode(String email, String code) {
        // Rate Limiting 체크
        if (isRateLimited(email)) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS);
        }

        String key = VERIFY_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION, TimeUnit.MINUTES);

        // Rate Limit 설정
        setRateLimit(email);
    }

    /**
     * 인증 코드 검증
     * @param email 이메일 주소
     * @param code 사용자가 입력한 코드
     * @return 검증 성공 여부
     */
    public boolean verifyCode(String email, String code) {
        String key = VERIFY_CODE_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new BusinessException(ResultCode.INVALID_VERIFICATION_CODE);
        }

        // 검증 성공 시 코드 삭제 (재사용 방지)
        redisTemplate.delete(key);
        return true;
    }

    /**
     * 리셋 토큰 생성 및 저장
     * @param email 이메일 주소
     * @return 생성된 리셋 토큰
     */
    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        String key = RESET_TOKEN_PREFIX + token;

        redisTemplate.opsForValue().set(key, email, TOKEN_EXPIRATION, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 리셋 토큰 검증 및 이메일 조회
     * @param token 리셋 토큰
     * @return 토큰에 연결된 이메일 주소
     */
    public String validateResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        String email = redisTemplate.opsForValue().get(key);

        if (email == null) {
            throw new BusinessException(ResultCode.INVALID_RESET_TOKEN);
        }

        return email;
    }

    /**
     * 리셋 토큰 삭제
     * @param token 리셋 토큰
     */
    public void deleteResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }

    /**
     * Rate Limiting 체크
     */
    private boolean isRateLimited(String email) {
        String key = RATE_LIMIT_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Rate Limit 설정
     */
    private void setRateLimit(String email) {
        String key = RATE_LIMIT_PREFIX + email;
        redisTemplate.opsForValue().set(key, "1", RATE_LIMIT_DURATION, TimeUnit.MINUTES);
    }
}
