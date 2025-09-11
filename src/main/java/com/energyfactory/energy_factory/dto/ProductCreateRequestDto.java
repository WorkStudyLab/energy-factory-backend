package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "상품 생성 요청 DTO")
public class ProductCreateRequestDto {

    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 255, message = "상품명은 255자 이하여야 합니다")
    @Schema(description = "상품명", example = "한우 등심 500g", required = true)
    private String name;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
    @Schema(description = "가격", example = "29900.00", required = true)
    private BigDecimal price;

    @NotBlank(message = "카테고리는 필수입니다")
    @Size(max = 100, message = "카테고리는 100자 이하여야 합니다")
    @Schema(description = "카테고리", example = "고기", required = true)
    private String category;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Size(max = 100, message = "브랜드명은 100자 이하여야 합니다")
    @Schema(description = "브랜드명", example = "프리미엄 한우")
    private String brand;

    @DecimalMin(value = "0", message = "중량은 0 이상이어야 합니다")
    @Schema(description = "중량", example = "500.00")
    private BigDecimal weight;

    @NotBlank(message = "중량 단위는 필수입니다")
    @Size(max = 10, message = "중량 단위는 10자 이하여야 합니다")
    @Schema(description = "중량 단위", example = "g", required = true)
    private String weightUnit;

    @Schema(description = "상품 설명", example = "신선하고 맛있는 한우 등심입니다.")
    private String description;

    @NotNull(message = "재고는 필수입니다")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    @Schema(description = "재고 수량", example = "100", required = true)
    private Long stock;

    @NotBlank(message = "판매 상태는 필수입니다")
    @Schema(description = "판매 상태", example = "AVAILABLE", required = true)
    private String status;

    @Size(max = 100, message = "보관 방법은 100자 이하여야 합니다")
    @Schema(description = "보관 방법", example = "냉장보관")
    private String storage;

    @Schema(description = "태그 ID 목록", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @Schema(description = "영양성분 정보")
    private List<NutrientDto> nutrients;

    @Getter
    @Setter
    @Schema(description = "영양성분 정보")
    public static class NutrientDto {
        @NotBlank(message = "영양소명은 필수입니다")
        @Size(max = 100, message = "영양소명은 100자 이하여야 합니다")
        @Schema(description = "영양소명", example = "단백질", required = true)
        private String name;

        @NotBlank(message = "함량은 필수입니다")
        @Size(max = 50, message = "함량은 50자 이하여야 합니다")
        @Schema(description = "함량", example = "25.5", required = true)
        private String value;

        @NotBlank(message = "단위는 필수입니다")
        @Size(max = 20, message = "단위는 20자 이하여야 합니다")
        @Schema(description = "단위", example = "g", required = true)
        private String unit;
    }
}