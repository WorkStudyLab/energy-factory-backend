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
 * 실제 토스페이먼츠 API 응답 구조에 맞춤
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentResponseDto {

    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method;
    private BigDecimal totalAmount;
    private BigDecimal balanceAmount;
    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private String transactionKey;
    private String receiptUrl;

    // 카드 정보
    private Card card;

    // 가상계좌 정보
    private VirtualAccount virtualAccount;

    // 에러 정보
    private String code;
    private String message;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Card {
        private String company;
        private String number;
        private String installmentPlanMonths;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private String receiptUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VirtualAccount {
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private OffsetDateTime dueDate;
        private String refundStatus;
        private Boolean expired;
    }
}
