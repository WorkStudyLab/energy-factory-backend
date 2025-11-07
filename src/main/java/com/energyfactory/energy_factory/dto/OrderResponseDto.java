package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "주문 응답 DTO")
public class OrderResponseDto {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "주문번호", example = "20240101001")
    private Long orderNumber;

    @Schema(description = "주문 총 합계", example = "59800.00")
    private BigDecimal totalPrice;

    @Schema(description = "주문 상태", example = "PENDING")
    private String status;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private String paymentStatus;

    @Schema(description = "수령인", example = "홍길동")
    private String recipientName;

    @Schema(description = "수령인 전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "우편번호", example = "12345")
    private String postalCode;

    @Schema(description = "기본주소", example = "서울시 강남구 테헤란로 123")
    private String addressLine1;

    @Schema(description = "상세주소", example = "456호")
    private String addressLine2;

    @Schema(description = "주문 상품 목록")
    private List<OrderItemResponseDto> orderItems;

    @Schema(description = "결제 정보")
    private PaymentResponseDto payment;

    @Schema(description = "배송 정보")
    private DeliveryInfoDto deliveryInfo;

    @Getter
    @Builder
    @Schema(description = "주문 상품 정보")
    public static class OrderItemResponseDto {
        @Schema(description = "주문 상세 ID", example = "1")
        private Long id;

        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Schema(description = "상품명", example = "한우 등심 500g")
        private String productName;

        @Schema(description = "상품 이미지 URL", example = "https://example.com/image.jpg")
        private String productImageUrl;

        @Schema(description = "상품 변형 ID (옵션 선택)", example = "25")
        private Long variantId;

        @Schema(description = "상품 변형명 (옵션명)", example = "500g")
        private String variantName;

        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;

        @Schema(description = "단가", example = "29900.00")
        private BigDecimal price;

        @Schema(description = "상품별 총액", example = "59800.00")
        private BigDecimal totalPrice;

    }

    @Getter
    @Builder
    @Schema(description = "결제 정보")
    public static class PaymentResponseDto {
        @Schema(description = "결제 ID", example = "1")
        private Long id;

        @Schema(description = "결제 수단", example = "CREDIT_CARD")
        private String paymentMethod;

        @Schema(description = "결제 상태", example = "COMPLETED")
        private String paymentStatus;

        @Schema(description = "PG사 거래 ID", example = "pg_transaction_123")
        private String transactionId;

        @Schema(description = "결제 금액", example = "59800.00")
        private BigDecimal amount;

        @Schema(description = "결제 완료 시각", example = "2024-01-01T10:05:00")
        private LocalDateTime paidAt;

    }

    @Getter
    @Builder
    @Schema(description = "배송 정보")
    public static class DeliveryInfoDto {
        @Schema(description = "수령인", example = "홍길동")
        private String recipientName;

        @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123 456호")
        private String address;

        @Schema(description = "배송 완료 예정일", example = "2024-01-03T10:00:00")
        private LocalDateTime estimatedDeliveryDate;
    }
}