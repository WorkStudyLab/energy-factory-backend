package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "장바구니 아이템 응답 DTO")
public class CartItemResponseDto {

    @Schema(description = "장바구니 아이템 ID", example = "1")
    private Long id;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품명", example = "한우 등심")
    private String productName;

    @Schema(description = "상품 이미지 URL", example = "https://example.com/image.jpg")
    private String productImageUrl;

    @Schema(description = "상품 카테고리", example = "고기")
    private String productCategory;

    @Schema(description = "상품 변형 ID", example = "5")
    private Long variantId;

    @Schema(description = "상품 변형명 (옵션명)", example = "500g")
    private String variantName;

    @Schema(description = "단가", example = "29900.00")
    private BigDecimal price;

    @Schema(description = "수량", example = "2")
    private Integer quantity;

    @Schema(description = "총 금액 (단가 × 수량)", example = "59800.00")
    private BigDecimal totalPrice;

    @Schema(description = "재고", example = "100")
    private Long stock;

    @Schema(description = "재고 충분 여부", example = "true")
    private Boolean isAvailable;

    @Schema(description = "판매 상태", example = "AVAILABLE")
    private String productStatus;

    @Schema(description = "영양소 정보 (100g 기준)")
    private NutritionDto nutrition;

    @Schema(description = "상품 중량 (실제 판매 중량)", example = "500.0")
    private BigDecimal weight;

    @Schema(description = "중량 단위", example = "g")
    private String weightUnit;

    @Schema(description = "장바구니 담은 날짜", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
