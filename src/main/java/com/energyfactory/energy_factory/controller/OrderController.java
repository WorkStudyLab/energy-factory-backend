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

    @PostMapping("/from-cart")
    @Operation(
        summary = "장바구니 기반 주문 생성",
        description = "선택한 장바구니 아이템들로 주문을 생성합니다.\n\n" +
                     "- 재고를 자동으로 차감합니다\n" +
                     "- 주문 성공 시 장바구니에서 자동 삭제됩니다\n" +
                     "- 트랜잭션 처리로 일관성을 보장합니다"
    )
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderFromCartRequestDto request
    ) {
        Long userId = userDetails.getUser().getId();
        OrderResponseDto order = orderService.createOrderFromCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ResultCode.SUCCESS_POST, order));
    }

    @PostMapping("/test/create")
    @Operation(
        summary = "테스트용 주문 생성 (인증 불필요)",
        description = "결제 테스트를 위한 간단한 주문을 생성합니다. 실제 상품/배송지 없이도 동작합니다."
    )
    public ResponseEntity<ApiResponse<OrderResponseDto>> createTestOrder(
            @RequestParam(defaultValue = "3") Long userId,
            @RequestParam(defaultValue = "50000") Double amount,
            @RequestParam(defaultValue = "테스트 상품") String orderName
    ) {
        OrderResponseDto order = orderService.createTestOrder(userId, amount, orderName);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ResultCode.SUCCESS_POST, order));
    }
}