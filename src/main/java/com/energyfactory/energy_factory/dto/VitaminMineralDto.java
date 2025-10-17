package com.energyfactory.energy_factory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "비타민 & 미네랄 정보")
public class VitaminMineralDto {

    @Schema(description = "영양소명", example = "비타민 B1")
    private String name;

    @Schema(description = "함량", example = "0.5mg")
    private String amount;

    @Schema(description = "일일 권장 섭취량 대비 %", example = "45")
    private Integer daily;
}
