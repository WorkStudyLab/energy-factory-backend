package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {
    @Schema(description = "사용자 이름", example = "김진장", required = true)
    private String name;

    @Schema(description = "이메일 주소", example = "exam@example.com", required = true)
    private String email;

    @Schema(description = "비밀번호", example = "password123!", required = true)
    private String password;

    @Schema(description = "전화번호 (하이픈 포함)", example = "010-1234-5678", required = true)
    private String phoneNumber;

    @Schema(description = "생년월일 (YYYY-MM-DD 형식)", example = "1990-01-01", required = true)
    private LocalDate birthDate;

    @Schema(description = "기본 배송지 주소", example = "서울특별시 금천구 스타밸리", required = true)
    private String address;
}
