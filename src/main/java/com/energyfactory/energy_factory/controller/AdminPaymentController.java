package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.PaymentResponseDto;
import com.energyfactory.energy_factory.dto.PaymentStatusUpdateRequestDto;
import com.energyfactory.energy_factory.service.PaymentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@Tag(name = "Admin Payment", description = "결제 관리자 API")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @Autowired
    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(
        summary = "전체 결제 목록 조회",
        description = "모든 결제 내역을 조회합니다. 상태별, 결제수단별 필터링이 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
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
    public ResponseEntity<List<PaymentResponseDto>> getAllPayments(
            @Parameter(description = "결제 상태 필터", example = "COMPLETED")
            @RequestParam(required = false) String paymentStatus,
            
            @Parameter(description = "결제 수단 필터", example = "CREDIT_CARD")
            @RequestParam(required = false) String paymentMethod,
            
            @Parameter(description = "주문 ID 필터", example = "1")
            @RequestParam(required = false) Long orderId,
            
            @Parameter(description = "PG사 거래 ID 검색", example = "pg_transaction_123")
            @RequestParam(required = false) String transactionId,
            
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "결제 정보 조회",
        description = "특정 결제의 상세 정보를 조회합니다. (관리자용)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
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
            description = "결제 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<PaymentResponseDto> getPayment(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "결제 상태 변경",
        description = "결제 상태를 관리자가 직접 변경합니다. PG사 연동 실패 시 수동 처리용입니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상태 변경 성공",
            content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
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
            description = "결제 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<PaymentResponseDto> updatePaymentStatus(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "변경할 결제 상태 정보", required = true)
            @Valid @RequestBody PaymentStatusUpdateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refund")
    @Operation(
        summary = "관리자 환불 처리",
        description = "관리자가 결제를 환불 처리합니다. 고객 요청이나 상품 문제 시 사용됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "환불 성공",
            content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "환불할 수 없는 결제 상태",
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
            description = "결제 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "환불 사유", example = "상품 결함으로 인한 환불", required = true)
            @RequestParam String reason
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }
}