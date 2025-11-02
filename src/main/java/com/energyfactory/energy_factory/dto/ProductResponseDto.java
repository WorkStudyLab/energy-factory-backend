package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "상품 응답 DTO")
public class ProductResponseDto {

    @Schema(description = "상품 ID", example = "1")
    private Long id;

    @Schema(description = "상품명", example = "한우 등심 500g")
    private String name;

    @Schema(description = "가격", example = "29900.00")
    private BigDecimal price;

    @Schema(description = "카테고리", example = "고기")
    private String category;

    @Schema(description = "이미지 URL (대표 이미지)", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "이미지 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> images;

    @Schema(description = "브랜드명", example = "프리미엄 한우")
    private String brand;

    @Schema(description = "중량", example = "500.00")
    private BigDecimal weight;

    @Schema(description = "중량 단위", example = "g")
    private String weightUnit;

    @Schema(description = "상품 설명", example = "신선하고 맛있는 한우 등심입니다.")
    private String description;

    @Schema(description = "판매 상태", example = "AVAILABLE")
    private String status;

    @Schema(description = "보관 방법", example = "냉장보관")
    private String storage;


    @Schema(description = "평균 별점", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "리뷰 개수", example = "128")
    private Long reviewCount;

    @Schema(description = "태그 목록")
    private List<TagResponseDto> tags;

    @Schema(description = "영양 성분 정보")
    private NutritionDto nutrition;

    @Schema(description = "비타민 & 미네랄 목록")
    private List<VitaminMineralDto> vitaminsAndMinerals;

    @Schema(description = "피트니스 목표별 점수")
    private GoalScoresDto goalScores;

    @Schema(description = "할인 전 원가", example = "35000.00")
    private BigDecimal originalPrice;

    @Schema(description = "할인율 (%)", example = "15")
    private Integer discount;

    @Schema(description = "배송 정보")
    private ShippingInfoDto shipping;

    @Schema(description = "상품 옵션/변형 목록")
    private List<ProductVariantDto> variants;

    @Getter
    @Builder
    @Schema(description = "태그 정보")
    public static class TagResponseDto {
        @Schema(description = "태그 ID", example = "1")
        private Long id;

        @Schema(description = "태그명", example = "고단백")
        private String name;
    }
}