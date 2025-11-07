package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "마이페이지 사용자 응답 DTO")
public class UserResponseDto {

    @Schema(description = "사용자 이름", example = "김진장", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "이메일 주소", example = "exam@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "전화번호 (하이픈 포함)", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "생년월일 (YYYY-MM-DD 형식)", example = "1990-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate birthDate;

    @Schema(description = "소셜 로그인 제공자", example = "naver", allowableValues = {"local", "naver", "kakao", "google"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String authProvider;

    @Schema(description = "회원 가입일 (YYYY-MM-DD 형식)", example = "2024-06-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate memberSince;

    @Schema(description = "기본 배송지 주소", example = "서울특별시 금천구 스타밸리", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @Schema(description = "사용자 권한", example = "USER", allowableValues = {"USER", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}