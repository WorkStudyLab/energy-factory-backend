package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.PaymentResponseDto;
import com.energyfactory.energy_factory.service.PaymentService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 관리 컨트롤러
 * 주문별 결제 정보 조회 및 환불 처리 기능을 제공
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "주문별 결제 정보 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<PaymentResponseDto>> getPaymentByOrder(
            @PathVariable Long orderId
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "결제 정보 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<PaymentResponseDto>> getPayment(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "결제 환불")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<PaymentResponseDto>> refundPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}