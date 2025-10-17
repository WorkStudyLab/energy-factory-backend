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
@Schema(description = "영양 성분 정보")
public class NutritionDto {

    // ===== 주요 영양소 (필수) =====
    @Schema(description = "칼로리 (kcal, 100g 기준)", example = "250")
    private Integer calories;

    @Schema(description = "단백질 (g, 100g 기준)", example = "25.0")
    private BigDecimal protein;

    @Schema(description = "탄수화물 (g, 100g 기준)", example = "30.0")
    private BigDecimal carbs;

    @Schema(description = "지방 (g, 100g 기준)", example = "5.0")
    private BigDecimal fat;

    // ===== 상세 영양소 (옵셔널) =====
    @Schema(description = "포화지방 (g)", example = "1.5")
    private BigDecimal saturatedFat;

    @Schema(description = "트랜스지방 (g)", example = "0.0")
    private BigDecimal transFat;

    @Schema(description = "콜레스테롤 (mg)", example = "10")
    private Integer cholesterol;

    @Schema(description = "나트륨 (mg)", example = "200")
    private Integer sodium;

    @Schema(description = "식이섬유 (g)", example = "8.0")
    private BigDecimal fiber;

    @Schema(description = "당류 (g)", example = "5.0")
    private BigDecimal sugars;
}
