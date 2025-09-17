package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "주문 상태 변경 요청 DTO")
public class OrderStatusUpdateRequestDto {

    @NotBlank(message = "주문 상태는 필수입니다")
    @Schema(
        description = "주문 상태", 
        example = "CONFIRMED",
        required = true,
        allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "SHIPPED", "DELIVERED", "CANCELLED"}
    )
    private String status;

    @Schema(description = "상태 변경 사유", example = "고객 요청에 의한 취소")
    private String reason;
}