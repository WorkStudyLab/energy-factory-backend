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
@Schema(description = "배송 정보")
public class ShippingInfoDto {

    @Schema(description = "배송비", example = "3000.00")
    private BigDecimal fee;

    @Schema(description = "무료배송 기준 금액", example = "30000.00")
    private BigDecimal freeShippingThreshold;

    @Schema(description = "예상 배송 기간", example = "1-2일")
    private String estimatedDays;
}
