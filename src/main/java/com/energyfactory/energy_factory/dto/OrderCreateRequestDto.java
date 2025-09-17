package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "주문 생성 요청 DTO")
public class OrderCreateRequestDto {

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

    @NotBlank(message = "결제 수단은 필수입니다")
    @Schema(description = "결제 수단", example = "CREDIT_CARD", required = true)
    private String paymentMethod;

    @Valid
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    @Schema(description = "주문 상품 목록", required = true)
    private List<OrderItemCreateDto> orderItems;

    @Getter
    @Setter
    @Schema(description = "주문 상품 정보")
    public static class OrderItemCreateDto {
        @NotNull(message = "상품 ID는 필수입니다")
        @Positive(message = "상품 ID는 양수여야 합니다")
        @Schema(description = "상품 ID", example = "1", required = true)
        private Long productId;

        @NotNull(message = "주문 수량은 필수입니다")
        @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다")
        @Max(value = 999, message = "주문 수량은 999개 이하여야 합니다")
        @Schema(description = "주문 수량", example = "2", required = true)
        private Integer quantity;

        @NotNull(message = "단가는 필수입니다")
        @DecimalMin(value = "0", message = "단가는 0 이상이어야 합니다")
        @Schema(description = "단가", example = "29900.00", required = true)
        private BigDecimal price;
    }
}