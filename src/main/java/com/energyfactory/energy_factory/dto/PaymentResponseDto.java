package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "결제 응답 DTO")
public class PaymentResponseDto {

    @Schema(description = "결제 ID", example = "1")
    private Long id;

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @Schema(description = "주문번호", example = "20240101001")
    private Long orderNumber;

    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    private String paymentMethod;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private String paymentStatus;

    @Schema(description = "PG사 거래 ID", example = "pg_transaction_123")
    private String transactionId;

    @Schema(description = "결제 금액", example = "59800.00")
    private BigDecimal amount;

    @Schema(description = "결제 완료 시각", example = "2024-01-01T10:05:00")
    private LocalDateTime paidAt;

    @Schema(description = "생성일", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}