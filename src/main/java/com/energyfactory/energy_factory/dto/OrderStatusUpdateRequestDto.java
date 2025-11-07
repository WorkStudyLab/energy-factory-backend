package com.energyfactory.energy_factory.dto;

import com.energyfactory.energy_factory.utils.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequestDto {

    @NotNull(message = "주문 상태는 필수입니다")
    private OrderStatus status;
}
