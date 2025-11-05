package com.energyfactory.energy_factory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 승인 요청 DTO
 * 클라이언트에서는 paymentKey, orderId만 전송
 * 서버에서 amount를 주문 정보에서 가져와 토스페이먼츠 API에 전달
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토스페이먼츠 결제 승인 요청")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentConfirmRequestDto {

    @NotBlank(message = "결제 키는 필수입니다")
    @Schema(description = "결제 키 (토스페이먼츠에서 발급)", example = "tviva20240101000000ABC123")
    private String paymentKey;

    @NotBlank(message = "주문 ID는 필수입니다")
    @Schema(description = "주문 ID (주문번호)", example = "1762309163164")
    private String orderId;

    @Schema(hidden = true, description = "결제 금액 (서버에서 자동 설정)")
    private BigDecimal amount;
}
