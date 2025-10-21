package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "장바구니 목록 응답 DTO")
public class CartListResponseDto {

    @Schema(description = "장바구니 아이템 목록")
    private List<CartItemResponseDto> items;

    @Schema(description = "장바구니 아이템 개수", example = "3")
    private Integer itemCount;

    @Schema(description = "전체 상품 수량 합계", example = "7")
    private Integer totalQuantity;

    @Schema(description = "장바구니 총 금액", example = "149700.00")
    private BigDecimal totalPrice;

    @Schema(description = "배송비", example = "3000.00")
    private BigDecimal shippingFee;

    @Schema(description = "무료배송 기준 금액", example = "50000.00")
    private BigDecimal freeShippingThreshold;

    @Schema(description = "무료배송까지 남은 금액 (0이면 무료배송)", example = "0.00")
    private BigDecimal amountToFreeShipping;

    @Schema(description = "최종 결제 예정 금액 (상품 총액 + 배송비)", example = "152700.00")
    private BigDecimal finalPrice;

    @Schema(description = "장바구니 전체 영양소 합계 (차트용)")
    private NutritionSummaryDto nutritionSummary;
}
