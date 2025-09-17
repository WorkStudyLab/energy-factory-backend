package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "사용자 응답 DTO")
public class UserResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "소셜 로그인 제공자", example = "LOCAL")
    private String provider;

    @Schema(description = "권한", example = "USER")
    private String role;

    @Schema(description = "생성일", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "배송지 목록")
    private List<UserAddressResponseDto> addresses;

    @Getter
    @Builder
    @Schema(description = "사용자 배송지 정보")
    public static class UserAddressResponseDto {
        @Schema(description = "배송지 ID", example = "1")
        private Long id;

        @Schema(description = "수령인", example = "홍길동")
        private String recipientName;

        @Schema(description = "수령인 연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "우편번호", example = "12345")
        private String postalCode;

        @Schema(description = "기본주소", example = "서울시 강남구 테헤란로 123")
        private String addressLine1;

        @Schema(description = "상세주소", example = "456호")
        private String addressLine2;

        @Schema(description = "기본 배송지 여부", example = "true")
        private Boolean isDefault;

        @Schema(description = "생성일", example = "2024-01-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정일", example = "2024-01-01T10:00:00")
        private LocalDateTime updatedAt;
    }
}