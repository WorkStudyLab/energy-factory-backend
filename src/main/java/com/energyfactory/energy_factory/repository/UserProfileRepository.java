package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 프로필 데이터 접근 레포지토리
 * 기본 CRUD 기능을 제공
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 사용자 ID로 프로필 조회
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * 사용자 ID로 프로필 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}