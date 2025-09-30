package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.PaymentCreateRequestDto;
import com.energyfactory.energy_factory.dto.PaymentResponseDto;
import com.energyfactory.energy_factory.service.PaymentService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 관리 컨트롤러
 * 주문별 결제 정보 조회 및 환불 처리 기능을 제공
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 관련 API")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "결제 처리")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(
            @Valid @RequestBody PaymentCreateRequestDto requestDto
    ) {
        PaymentResponseDto payment = paymentService.processPayment(requestDto);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "주문별 결제 정보 조회")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByOrder(
            @PathVariable Long orderId
    ) {
        PaymentResponseDto payment = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }

    @GetMapping("/{id}")
    @Operation(summary = "결제 정보 조회")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPayment(
            @PathVariable Long id
    ) {
        PaymentResponseDto payment = paymentService.getPayment(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "결제 환불")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> refundPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        PaymentResponseDto payment = paymentService.refundPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }
}