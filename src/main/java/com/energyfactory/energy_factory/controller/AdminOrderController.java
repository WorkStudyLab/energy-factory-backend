package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Order", description = "주문 관리자 API")
public class AdminOrderController {

    private final OrderService orderService;

    @Autowired
    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(
        summary = "전체 주문 목록 조회",
        description = "모든 사용자의 주문 목록을 조회합니다. 다양한 필터링과 페이징이 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = OrderListResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<OrderListResponseDto> getAllOrders(
            @Parameter(description = "사용자 ID 필터", example = "1")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "주문 상태 필터", example = "PENDING")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "결제 상태 필터", example = "COMPLETED")
            @RequestParam(required = false) String paymentStatus,
            
            @Parameter(description = "주문번호 검색", example = "20240101001")
            @RequestParam(required = false) Long orderNumber,
            
            @Parameter(description = "수령인 이름 검색", example = "홍길동")
            @RequestParam(required = false) String recipientName,
            
            @Parameter(description = "페이징 정보")
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
        
        return ResponseEntity.ok(mockResponse);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "주문 상세 조회",
        description = "특정 주문의 상세 정보를 조회합니다. (관리자용)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<OrderResponseDto> getOrder(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "주문 상태 변경",
        description = "주문의 배송 상태를 변경합니다. (PENDING → CONFIRMED → PREPARING → SHIPPED → DELIVERED)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상태 변경 성공",
            content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 상태 값 또는 상태 변경 불가",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "변경할 상태 정보", required = true)
            @Valid @RequestBody OrderStatusUpdateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "주문 강제 취소",
        description = "관리자가 주문을 강제로 취소합니다. 환불 처리도 함께 진행됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "취소 성공",
            content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "취소할 수 없는 주문 상태",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "취소 사유", example = "상품 결함으로 인한 관리자 취소")
            @RequestParam String reason
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }
}