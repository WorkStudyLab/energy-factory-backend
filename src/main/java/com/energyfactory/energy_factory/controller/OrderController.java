package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.OrderCreateRequestDto;
import com.energyfactory.energy_factory.dto.OrderListResponseDto;
import com.energyfactory.energy_factory.dto.OrderResponseDto;
import com.energyfactory.energy_factory.service.OrderService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 관리 컨트롤러
 * 주문 생성, 조회, 취소 등의 기능을 제공
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(
        summary = "주문 목록 조회",
        description = "본인의 주문 목록을 조회합니다.\n\n" +
                     "- 주문 상태(status)와 결제 상태(paymentStatus)로 필터링 가능"
    )
    public ResponseEntity<ApiResponse<OrderListResponseDto>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = userDetails.getUser().getId();
        OrderListResponseDto orders = orderService.getOrders(userId, status, paymentStatus, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, orders));
    }

    @PostMapping
    @Operation(
        summary = "주문 생성",
        description = "주문을 생성합니다.\n\n" +
                     "- 배송지 정보와 주문 상품 정보 필요"
    )
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequestDto request
    ) {
        Long userId = userDetails.getUser().getId();
        OrderResponseDto order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ResultCode.SUCCESS_POST, order));
    }

    @GetMapping("/{orderNumber}")
    @Operation(
        summary = "주문 상세 조회",
        description = "본인의 주문 상세 정보를 주문번호로 조회합니다. 본인 소유가 아닌 주문 조회 시 403 에러 발생\n\n" +
                     "- orderNumber: 주문 번호 (예: 20251107001)"
    )
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderNumber
    ) {
        Long userId = userDetails.getUser().getId();
        OrderResponseDto order = orderService.getOrderByNumber(userId, orderNumber);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, order));
    }

    @PatchMapping("/{orderNumber}/cancel")
    @Operation(
        summary = "주문 취소",
        description = "본인의 주문을 취소합니다. 본인 소유가 아닌 주문 취소 시 403 에러 발생\n\n" +
                     "- orderNumber: 주문 번호 (예: 20251107001)"
    )
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderNumber,
            @RequestParam(required = false) String reason
    ) {
        Long userId = userDetails.getUser().getId();
        OrderResponseDto order = orderService.cancelOrderByNumber(userId, orderNumber, reason);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, order));
    }
}