package com.energyfactory.energy_factory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 소셜 로그인 후 추가 정보 입력 요청 DTO
 * 생년월일, 배송지 정보를 받습니다. (전화번호는 네이버에서 필수로 받음)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdditionalInfoRequestDto {

    // 생년월일 (필수)
    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;

    // 배송지 주소 (필수)
    @NotBlank(message = "배송지 주소는 필수입니다.")
    private String address;
}
