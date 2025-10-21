package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "장바구니 전체 영양소 합계 정보 (차트용)")
public class NutritionSummaryDto {

    @Schema(description = "총 칼로리 (kcal)", example = "2450.5")
    private BigDecimal totalCalories;

    @Schema(description = "단백질 칼로리 비율 (%) - 총 칼로리 대비 단백질이 차지하는 칼로리 비율 (단백질 1g = 4kcal)",
            example = "35.5")
    private BigDecimal proteinRatio;

    @Schema(description = "탄수화물 칼로리 비율 (%) - 총 칼로리 대비 탄수화물이 차지하는 칼로리 비율 (탄수화물 1g = 4kcal)",
            example = "45.0")
    private BigDecimal carbsRatio;

    @Schema(description = "지방 칼로리 비율 (%) - 총 칼로리 대비 지방이 차지하는 칼로리 비율 (지방 1g = 9kcal)",
            example = "19.5")
    private BigDecimal fatRatio;
}
