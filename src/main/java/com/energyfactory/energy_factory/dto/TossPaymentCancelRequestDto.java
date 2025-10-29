package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 취소 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토스페이먼츠 결제 취소 요청")
public class TossPaymentCancelRequestDto {

    @Schema(description = "취소 사유", example = "고객 요청")
    private String cancelReason;

    @Schema(description = "부분 취소 금액 (null이면 전체 취소)", example = "10000")
    private BigDecimal cancelAmount;

    @Schema(description = "환불 계좌 은행 코드")
    private String refundReceiveAccount;
}
