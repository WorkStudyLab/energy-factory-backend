package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "상품 목록 응답 DTO")
public class ProductListResponseDto {

    @Schema(description = "상품 목록")
    private List<ProductSummaryDto> products;

    @Schema(description = "페이징 정보")
    private PageInfoDto pageInfo;

    @Getter
    @Builder
    @Schema(description = "상품 요약 정보 (목록용)")
    public static class ProductSummaryDto {
        @Schema(description = "상품 ID", example = "1")
        private Long id;

        @Schema(description = "상품명", example = "한우 등심 500g")
        private String name;

        @Schema(description = "가격", example = "29900.00")
        private java.math.BigDecimal price;

        @Schema(description = "카테고리", example = "고기")
        private String category;

        @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
        private String imageUrl;

        @Schema(description = "브랜드명", example = "프리미엄 한우")
        private String brand;

        @Schema(description = "중량", example = "500.00")
        private java.math.BigDecimal weight;

        @Schema(description = "중량 단위", example = "g")
        private String weightUnit;

        @Schema(description = "판매 상태", example = "AVAILABLE")
        private String status;

        @Schema(description = "평균 별점", example = "4.5")
        private java.math.BigDecimal averageRating;

        @Schema(description = "리뷰 개수", example = "128")
        private Long reviewCount;

        @Schema(description = "태그 목록", example = "[\"고단백\", \"다이어트\"]")
        private List<String> tags;

        @Schema(description = "할인 전 원가", example = "35000.00")
        private java.math.BigDecimal originalPrice;

        @Schema(description = "할인율 (%)", example = "15")
        private Integer discount;

        @Schema(description = "상품 옵션/변형 목록")
        private List<ProductVariantDto> variants;
    }

    @Getter
    @Builder
    @Schema(description = "페이징 정보")
    public static class PageInfoDto {
        @Schema(description = "현재 페이지", example = "1")
        private int currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private int pageSize;

        @Schema(description = "전체 요소 수", example = "150")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "8")
        private int totalPages;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean last;
    }
}