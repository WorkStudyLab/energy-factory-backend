package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
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
    @Operation(summary = "주문 목록 조회")
    public ResponseEntity<ApiResponse<OrderListResponseDto>> getOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        OrderListResponseDto orders = orderService.getOrders(userId, status, paymentStatus, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, orders));
    }

    @PostMapping
    @Operation(summary = "주문 생성")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @RequestParam Long userId,
            @Valid @RequestBody OrderCreateRequestDto request
    ) {
        OrderResponseDto order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ResultCode.SUCCESS_POST, order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "주문 상세 조회")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(
            @RequestParam Long userId,
            @PathVariable Long id
    ) {
        OrderResponseDto order = orderService.getOrder(userId, id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, order));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "주문 취소")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @RequestParam Long userId,
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        OrderResponseDto order = orderService.cancelOrder(userId, id, reason);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "주문번호로 주문 조회")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderByNumber(
            @RequestParam Long userId,
            @PathVariable Long orderNumber
    ) {
        OrderResponseDto order = orderService.getOrderByNumber(userId, orderNumber);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, order));
    }
}