package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.OrderResponseDto;
import com.energyfactory.energy_factory.dto.OrderStatusUpdateRequestDto;
import com.energyfactory.energy_factory.service.OrderService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 주문 관리 컨트롤러
 * 관리자가 주문 상태를 변경하는 기능 제공
 */
@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Order", description = "관리자 주문 관리 API")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PatchMapping("/{orderId}/status")
    @Operation(
            summary = "주문 상태 변경 (관리자)",
            description = "관리자가 주문 상태를 변경합니다.\n\n" +
                    "**주문 상태:**\n" +
                    "- PENDING: 주문접수\n" +
                    "- CONFIRMED: 주문확인\n" +
                    "- SHIPPED: 배송중\n" +
                    "- DELIVERED: 배송완료\n" +
                    "- CANCELLED: 주문취소\n\n" +
                    "**알림 발송:**\n" +
                    "- 상태가 CONFIRMED, SHIPPED, DELIVERED, CANCELLED로 변경되면\n" +
                    "- 해당 주문의 고객에게 SSE 실시간 알림이 자동 전송됩니다.\n\n" +
                    "**참고:** 현재는 인증 없이 동작하지만, 실제 운영 시 관리자 권한 검증 필요"
    )
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDto request
    ) {
        OrderResponseDto order = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, order));
    }
}
