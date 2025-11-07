package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "주문 목록 응답 DTO")
public class OrderListResponseDto {

    @Schema(description = "주문 목록")
    private List<OrderSummaryDto> orders;

    @Schema(description = "페이징 정보")
    private PageInfoDto pageInfo;

    @Getter
    @Builder
    @Schema(description = "주문 요약 정보")
    public static class OrderSummaryDto {
        @Schema(description = "주문 ID", example = "1")
        private Long id;

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

        @Schema(description = "주문 상품 수량", example = "3")
        private Integer itemCount;

        @Schema(description = "대표 상품명", example = "한우 등심 500g")
        private String representativeProductName;

        @Schema(description = "대표 상품 이미지 URL", example = "https://example.com/image.jpg")
        private String representativeProductImageUrl;

        @Schema(description = "배송 완료 예정일", example = "2024-01-03T10:00:00")
        private LocalDateTime estimatedDeliveryDate;

        @Schema(description = "결제 정보")
        private PaymentDto payment;

        @Schema(description = "배송 정보")
        private DeliveryDto delivery;

    }

    @Getter
    @Builder
    @Schema(description = "결제 정보")
    public static class PaymentDto {
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
    public static class DeliveryDto {
        @Schema(description = "수령인", example = "홍길동")
        private String recipientName;

        @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123 456호")
        private String address;

        @Schema(description = "배송 완료 예정일", example = "2024-01-03T10:00:00")
        private LocalDateTime estimatedDeliveryDate;
    }

    @Getter
    @Builder
    @Schema(description = "페이징 정보")
    public static class PageInfoDto {
        @Schema(description = "현재 페이지", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private Integer pageSize;

        @Schema(description = "전체 요소 수", example = "150")
        private Long totalElements;

        @Schema(description = "전체 페이지 수", example = "8")
        private Integer totalPages;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private Boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean last;
    }
}