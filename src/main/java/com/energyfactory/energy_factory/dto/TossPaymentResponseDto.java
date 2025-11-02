package com.energyfactory.energy_factory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 토스페이먼츠 API 응답 DTO
 * 결제 승인/취소에 필요한 필수 필드만 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentResponseDto {

    // 필수 필드
    private String paymentKey;
    private String orderId;
    private String method;
    private BigDecimal totalAmount;
    private String status;
    private OffsetDateTime approvedAt;

    // 간편결제 정보
    private EasyPay easyPay;

    // 에러 정보
    private String code;
    private String message;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EasyPay {
        private String provider;  // "토스페이", "카카오페이", "네이버페이" 등
        private BigDecimal amount;
        private BigDecimal discountAmount;
    }
}
