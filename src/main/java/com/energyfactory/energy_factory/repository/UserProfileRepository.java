package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 프로필 데이터 접근 레포지토리
 * 프로필 조회, 검색, 필터링 기능을 위한 쿼리 메서드 제공
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 사용자 ID로 프로필 조회
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * 사용자 엔티티로 프로필 조회
     */
    Optional<UserProfile> findByUser(User user);

    /**
     * 사용자 ID로 프로필 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 공개 프로필만 조회 (페이징)
     */
    Page<UserProfile> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 위치별 공개 프로필 조회
     */
    Page<UserProfile> findByLocationContainingIgnoreCaseAndIsPublicTrueOrderByCreatedAtDesc(
            String location, Pageable pageable);

    /**
     * 자기소개에 키워드가 포함된 공개 프로필 조회
     */
    Page<UserProfile> findByBioContainingIgnoreCaseAndIsPublicTrueOrderByCreatedAtDesc(
            String keyword, Pageable pageable);

    /**
     * 프로필 이미지가 있는 공개 프로필 조회
     */
    Page<UserProfile> findByProfileImageUrlIsNotNullAndIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 최근 가입한 공개 프로필 조회
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isPublic = true AND up.createdAt >= :fromDate ORDER BY up.createdAt DESC")
    List<UserProfile> findRecentPublicProfiles(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 생일이 특정 월인 공개 프로필 조회
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isPublic = true AND MONTH(up.birthDate) = :month ORDER BY DAY(up.birthDate)")
    List<UserProfile> findByBirthMonth(@Param("month") int month);

    /**
     * 관심사에 특정 키워드가 포함된 공개 프로필 조회
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isPublic = true AND up.interests LIKE %:keyword% ORDER BY up.createdAt DESC")
    Page<UserProfile> findByInterestsContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 웹사이트가 있는 공개 프로필 조회
     */
    Page<UserProfile> findByWebsiteUrlIsNotNullAndIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 기간 내에 생성된 프로필 조회
     */
    @Query("SELECT up FROM UserProfile up WHERE up.createdAt BETWEEN :startDate AND :endDate ORDER BY up.createdAt DESC")
    List<UserProfile> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 통계: 공개 프로필 수
     */
    long countByIsPublicTrue();

    /**
     * 통계: 프로필 이미지가 있는 공개 프로필 수
     */
    long countByProfileImageUrlIsNotNullAndIsPublicTrue();

    /**
     * 복합 검색: 위치와 관심사로 프로필 검색
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isPublic = true " +
           "AND (:location IS NULL OR up.location LIKE %:location%) " +
           "AND (:interests IS NULL OR up.interests LIKE %:interests%) " +
           "ORDER BY up.createdAt DESC")
    Page<UserProfile> findByLocationAndInterests(@Param("location") String location, 
                                                @Param("interests") String interests, 
                                                Pageable pageable);
}