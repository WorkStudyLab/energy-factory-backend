package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_profiles")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '프로필 ID'")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, columnDefinition = "BIGINT NOT NULL COMMENT 'FK 사용자 ID'")
    private User user;

    @Column(name = "profile_image_url", columnDefinition = "VARCHAR(500) COMMENT '프로필 이미지 URL'")
    private String profileImageUrl;

    @Column(name = "bio", columnDefinition = "TEXT COMMENT '자기소개'")
    private String bio;

    @Column(name = "interests", columnDefinition = "JSON COMMENT '관심사 목록'")
    private String interests;

    @Column(name = "preferences", columnDefinition = "JSON COMMENT '사용자 선호도 설정'")
    private String preferences;

    @Column(name = "is_public", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE COMMENT '프로필 공개 여부'")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "website_url", columnDefinition = "VARCHAR(500) COMMENT '웹사이트 URL'")
    private String websiteUrl;

    @Column(name = "location", columnDefinition = "VARCHAR(100) COMMENT '거주지'")
    private String location;

    @Column(name = "birth_date", columnDefinition = "DATE COMMENT '생년월일'")
    private java.time.LocalDate birthDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    // 비즈니스 메서드

    /**
     * UserProfile 엔티티 팩토리 메서드
     */
    public static UserProfile createProfile(User user) {
        return UserProfile.builder()
                .user(user)
                .isPublic(true)
                .build();
    }

    /**
     * 프로필 정보 업데이트
     */
    public void updateProfile(String profileImageUrl, String bio, String interests, 
                             String preferences, Boolean isPublic, String websiteUrl, 
                             String location, java.time.LocalDate birthDate) {
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (interests != null) {
            this.interests = interests;
        }
        if (preferences != null) {
            this.preferences = preferences;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
        if (websiteUrl != null) {
            this.websiteUrl = websiteUrl;
        }
        if (location != null) {
            this.location = location;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
    }

    /**
     * 프로필 공개 설정 토글
     */
    public void toggleVisibility() {
        this.isPublic = !this.isPublic;
    }

    /**
     * 프로필 이미지 업데이트
     */
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 자기소개 업데이트
     */
    public void updateBio(String bio) {
        this.bio = bio;
    }
}