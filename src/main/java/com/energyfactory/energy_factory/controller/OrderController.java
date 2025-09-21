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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        OrderListResponseDto mockResponse = OrderListResponseDto.builder()
                .orders(java.util.List.of())
                .pageInfo(OrderListResponseDto.PageInfoDto.builder()
                        .currentPage(0)
                        .pageSize(20)
                        .totalElements(0L)
                        .totalPages(0)
                        .first(true)
                        .last(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, mockResponse));
    }

    @PostMapping
    @Operation(summary = "주문 생성")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @Valid @RequestBody OrderCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ResultCode.SUCCESS_POST, null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "주문 상세 조회")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "주문 취소")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "주문번호로 주문 조회")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderByNumber(
            @PathVariable Long orderNumber
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }
}