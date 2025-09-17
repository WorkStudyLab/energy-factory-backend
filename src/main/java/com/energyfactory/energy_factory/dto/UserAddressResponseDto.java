package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "사용자 배송지 응답 DTO")
public class UserAddressResponseDto {

    @Schema(description = "배송지 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

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