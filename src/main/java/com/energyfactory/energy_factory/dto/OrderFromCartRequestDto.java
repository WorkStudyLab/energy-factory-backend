package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "장바구니 기반 주문 생성 요청 DTO")
public class OrderFromCartRequestDto {

    @NotEmpty(message = "주문할 장바구니 아이템 ID 목록은 필수입니다")
    @Schema(description = "주문할 장바구니 아이템 ID 목록", example = "[1, 2, 3]", required = true)
    private List<Long> cartItemIds;

    @NotBlank(message = "수령인은 필수입니다")
    @Size(max = 100, message = "수령인은 100자 이하여야 합니다")
    @Schema(description = "수령인", example = "홍길동", required = true)
    private String recipientName;

    @NotBlank(message = "수령인 전화번호는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
    @Schema(description = "수령인 전화번호", example = "010-1234-5678", required = true)
    private String phoneNumber;

    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
    @Schema(description = "우편번호", example = "12345", required = true)
    private String postalCode;

    @NotBlank(message = "기본주소는 필수입니다")
    @Schema(description = "기본주소", example = "서울시 강남구 테헤란로 123", required = true)
    private String addressLine1;

    @Schema(description = "상세주소", example = "456호")
    private String addressLine2;
}
