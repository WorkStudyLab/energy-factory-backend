package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 배송지 생성 요청 DTO")
public class UserAddressCreateRequestDto {

    @NotBlank(message = "수령인은 필수입니다")
    @Size(max = 100, message = "수령인은 100자 이하여야 합니다")
    @Schema(description = "수령인", example = "홍길동", required = true)
    private String recipientName;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
    @Schema(description = "수령인 연락처", example = "010-1234-5678")
    private String phone;

    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
    @Schema(description = "우편번호", example = "12345", required = true)
    private String postalCode;

    @NotBlank(message = "기본주소는 필수입니다")
    @Schema(description = "기본주소", example = "서울시 강남구 테헤란로 123", required = true)
    private String addressLine1;

    @Schema(description = "상세주소", example = "456호")
    private String addressLine2;

    @Schema(description = "기본 배송지 여부", example = "false")
    private Boolean isDefault = false;
}