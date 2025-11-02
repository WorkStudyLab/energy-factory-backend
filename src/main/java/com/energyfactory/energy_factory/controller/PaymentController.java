package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.config.TossPaymentsConfig;
import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.PaymentService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 결제 관리 컨트롤러
 * 주문별 결제 정보 조회 및 환불 처리 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 관련 API")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentsConfig tossPaymentsConfig;

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

    // ==================== 토스페이먼츠 API ====================

    @PostMapping("/toss/confirm")
    @Operation(
        summary = "토스페이먼츠 결제 승인",
        description = "프론트엔드에서 받은 paymentKey, orderId, amount로 실제 결제를 승인합니다.\n\n" +
                     "결제 위젯에서 결제 성공 시 리다이렉트된 페이지에서 이 API를 호출하면 됩니다."
    )
    public ResponseEntity<ApiResponse<PaymentResponseDto>> confirmTossPayment(
            @Valid @RequestBody TossPaymentConfirmRequestDto confirmRequest
    ) {
        log.info("토스페이먼츠 결제 승인 요청 - orderId: {}, amount: {}",
                confirmRequest.getOrderId(), confirmRequest.getAmount());

        PaymentResponseDto payment = paymentService.confirmTossPayment(confirmRequest);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }

    @PostMapping("/toss/{id}/cancel")
    @Operation(
        summary = "토스페이먼츠 결제 취소",
        description = "토스페이먼츠를 통해 결제된 건을 취소하고 환불 처리합니다."
    )
    public ResponseEntity<ApiResponse<PaymentResponseDto>> cancelTossPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        log.info("토스페이먼츠 결제 취소 요청 - paymentId: {}, reason: {}", id, reason);

        PaymentResponseDto payment = paymentService.cancelTossPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }
}