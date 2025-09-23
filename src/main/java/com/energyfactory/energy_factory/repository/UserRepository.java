package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 데이터 접근 레포지토리
 * 사용자 조회, 중복 체크, 인증 관련 기능을 위한 쿼리 메서드 제공
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);

    /**
     * 전화번호 중복 체크
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);



    /**
     * 소셜 로그인용 Provider + ProviderId로 사용자 조회
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

}
