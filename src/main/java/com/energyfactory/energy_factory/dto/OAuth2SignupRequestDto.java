package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * OAuth2 소셜 로그인 후 회원가입 완료 요청 DTO
 * 네이버에서 가져온 정보(자동 완성) + 사용자가 입력/수정한 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 회원가입 완료 요청 DTO")
public class OAuth2SignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일 주소 (네이버에서 자동 완성, 수정 가능)", example = "user@naver.com", required = true)
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "사용자 이름 (네이버에서 자동 완성, 수정 가능)", example = "홍길동", required = true)
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Schema(description = "전화번호 (네이버에서 자동 완성, 수정 가능)", example = "010-1234-5678", required = true)
    private String phoneNumber;

    @NotNull(message = "생년월일은 필수입니다.")
    @Schema(description = "생년월일 (YYYY-MM-DD 형식)", example = "1990-01-01", required = true)
    private LocalDate birthDate;

    @NotBlank(message = "주소는 필수입니다.")
    @Schema(description = "기본 배송지 주소", example = "서울특별시 금천구 스타밸리", required = true)
    private String address;
}
