package com.energyfactory.energy_factory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "상품 옵션/변형 정보")
public class ProductVariantDto {

    @Schema(description = "변형 ID", example = "1")
    private Long id;

    @Schema(description = "변형 이름 (예: 500g, 1kg)", example = "500g")
    private String name;

    @Schema(description = "해당 변형의 가격", example = "29900.00")
    private BigDecimal price;

    @Schema(description = "총 재고", example = "50")
    private Long stock;

    @Schema(description = "예약된 재고 (결제 진행 중)", example = "5")
    private Long reservedStock;

    @Schema(description = "판매 가능한 재고 (총재고 - 예약재고)", example = "45")
    private Long availableStock;
}
