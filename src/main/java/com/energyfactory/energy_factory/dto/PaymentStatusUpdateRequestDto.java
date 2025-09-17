package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결제 상태 변경 요청 DTO")
public class PaymentStatusUpdateRequestDto {

    @NotBlank(message = "결제 상태는 필수입니다")
    @Schema(
        description = "결제 상태", 
        example = "COMPLETED",
        required = true,
        allowableValues = {"PENDING", "COMPLETED", "FAILED", "CANCELLED", "REFUNDED"}
    )
    private String paymentStatus;

    @Schema(description = "PG사 거래 ID", example = "pg_transaction_456")
    private String transactionId;

    @Schema(description = "상태 변경 사유", example = "PG사 결제 승인 완료")
    private String reason;
}