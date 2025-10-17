package com.energyfactory.energy_factory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@Schema(description = "피트니스 목표별 점수 (0.0-5.0)")
public class GoalScoresDto {

    @Schema(description = "근육 증가 점수", example = "4.5")
    private BigDecimal muscleGain;

    @Schema(description = "체중 감량 점수", example = "3.8")
    private BigDecimal weightLoss;

    @Schema(description = "에너지 향상 점수", example = "4.2")
    private BigDecimal energy;

    @Schema(description = "회복 촉진 점수", example = "4.0")
    private BigDecimal recovery;

    @Schema(description = "전반적 건강 점수", example = "4.5")
    private BigDecimal health;
}
