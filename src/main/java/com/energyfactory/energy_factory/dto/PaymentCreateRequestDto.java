package com.energyfactory.energy_factory.dto;

import com.energyfactory.energy_factory.utils.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "결제 생성 요청 DTO")
public class PaymentCreateRequestDto {

    @NotNull(message = "주문 ID는 필수입니다")
    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @NotNull(message = "결제 수단은 필수입니다")
    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @NotNull(message = "결제 금액은 필수입니다")
    @Positive(message = "결제 금액은 0보다 커야 합니다")
    @Schema(description = "결제 금액", example = "59800.00")
    private BigDecimal amount;
}