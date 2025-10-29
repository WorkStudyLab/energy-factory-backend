package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 승인 요청 DTO
 * 결제위젯에서 받은 정보로 실제 결제를 승인
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토스페이먼츠 결제 승인 요청")
public class TossPaymentConfirmRequestDto {

    @NotBlank(message = "결제 키는 필수입니다")
    @Schema(description = "결제 키 (토스페이먼츠에서 발급)", example = "tviva20240101000000ABC123")
    private String paymentKey;

    @NotBlank(message = "주문 ID는 필수입니다")
    @Schema(description = "주문 ID", example = "1")
    private String orderId;

    @NotNull(message = "결제 금액은 필수입니다")
    @Schema(description = "결제 금액", example = "50000")
    private BigDecimal amount;
}
