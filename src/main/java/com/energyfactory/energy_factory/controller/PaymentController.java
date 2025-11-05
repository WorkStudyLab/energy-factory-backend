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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


    @GetMapping("/{id}")
    @Operation(
        summary = "결제 정보 조회",
        description = "본인의 결제 정보를 조회합니다. 본인 소유가 아닌 결제 조회 시 403 에러 발생\n\n" +
                     "- id: 결제 ID (paymentId)"
    )
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUser().getId();
        PaymentResponseDto payment = paymentService.getPayment(userId, id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }

    // ==================== 토스페이먼츠 API ====================

    @PostMapping("/toss/confirm")
    @Operation(
        summary = "토스페이먼츠 결제 승인",
        description = "프론트엔드에서 받은 paymentKey, orderId로 실제 결제를 승인합니다.\n\n" +
                     "결제 금액은 서버에서 주문 정보를 조회하여 자동으로 설정됩니다.\n\n" +
                     "결제 위젯에서 결제 성공 시 리다이렉트된 페이지에서 이 API를 호출하면 됩니다."
    )
    public ResponseEntity<ApiResponse<PaymentResponseDto>> confirmTossPayment(
            @Valid @RequestBody TossPaymentConfirmRequestDto confirmRequest
    ) {
        log.info("토스페이먼츠 결제 승인 요청 - orderId: {}",
                confirmRequest.getOrderId());

        PaymentResponseDto payment = paymentService.confirmTossPayment(confirmRequest);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }

    @PostMapping("/toss/{id}/cancel")
    @Operation(
        summary = "토스페이먼츠 결제 취소",
        description = "본인의 결제를 취소하고 환불 처리합니다. 본인 소유가 아닌 결제 취소 시 403 에러 발생\n\n" +
                     "- id: 결제 ID (paymentId)\n" +
                     "- 취소 성공 시 재고 자동 복원"
    )
    public ResponseEntity<ApiResponse<PaymentResponseDto>> cancelTossPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        Long userId = userDetails.getUser().getId();
        log.info("토스페이먼츠 결제 취소 요청 - userId: {}, paymentId: {}, reason: {}", userId, id, reason);

        PaymentResponseDto payment = paymentService.cancelTossPayment(userId, id, reason);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, payment));
    }
}