package com.energyfactory.energy_factory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequestDto {

    @NotBlank(message = "상품명은 필수입니다")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다")
    private String category;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;

    private String brand;

    @DecimalMin(value = "0.0", message = "할인 전 원가는 0 이상이어야 합니다")
    private BigDecimal originalPrice;

    @Min(value = 0, message = "할인율은 0 이상이어야 합니다")
    @Max(value = 100, message = "할인율은 100 이하여야 합니다")
    private Integer discount;

    @DecimalMin(value = "0.0", message = "중량은 0 이상이어야 합니다")
    private BigDecimal weight;

    @NotBlank(message = "중량 단위는 필수입니다")
    private String weightUnit;

    private String status;

    private String imageUrl;

    private String description;

    private String storage;

    private List<String> tags;

    // 배송 정보
    @DecimalMin(value = "0.0", message = "배송비는 0 이상이어야 합니다")
    private BigDecimal shippingFee;

    @DecimalMin(value = "0.0", message = "무료배송 기준 금액은 0 이상이어야 합니다")
    private BigDecimal freeShippingThreshold;

    private String estimatedDeliveryDays;

    // 피트니스 목표별 점수 (0.0 ~ 5.0)
    @DecimalMin(value = "0.0", message = "점수는 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "점수는 5.0 이하여야 합니다")
    private BigDecimal scoreMuscleGain;

    @DecimalMin(value = "0.0", message = "점수는 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "점수는 5.0 이하여야 합니다")
    private BigDecimal scoreWeightLoss;

    @DecimalMin(value = "0.0", message = "점수는 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "점수는 5.0 이하여야 합니다")
    private BigDecimal scoreEnergy;

    @DecimalMin(value = "0.0", message = "점수는 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "점수는 5.0 이하여야 합니다")
    private BigDecimal scoreRecovery;

    @DecimalMin(value = "0.0", message = "점수는 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "점수는 5.0 이하여야 합니다")
    private BigDecimal scoreHealth;
}
