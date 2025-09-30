package com.energyfactory.energy_factory.entity;

import com.energyfactory.energy_factory.utils.enums.PaymentMethod;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '결제 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '주문 ID'")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, columnDefinition = "VARCHAR(50) NOT NULL COMMENT '결제 수단'")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, columnDefinition = "VARCHAR(50) NOT NULL COMMENT '결제 상태'")
    private PaymentStatus paymentStatus;

    @Column(name = "transaction_id", unique = true, columnDefinition = "VARCHAR(255) COMMENT 'PG사 거래 ID'")
    private String transactionId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) NOT NULL COMMENT '결제 금액'")
    private BigDecimal amount;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP COMMENT '결제 완료 시각'")
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    // 비즈니스 메서드

    /**
     * Mock PG 거래 ID 생성
     * 실제 PG사 연동 시 실제 거래 ID로 대체
     */
    public static String generateMockTransactionId() {
        return "mock_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 결제 완료 처리
     */
    public void completePayment() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
        
        // Mock transaction ID 설정 (실제 PG에서는 응답값 사용)
        if (this.transactionId == null) {
            this.transactionId = generateMockTransactionId();
        }
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment() {
        this.paymentStatus = PaymentStatus.FAILED;
        this.paidAt = null;
    }

    /**
     * 환불 처리
     */
    public void refund() {
        if (this.paymentStatus != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 환불할 수 있습니다.");
        }
        this.paymentStatus = PaymentStatus.REFUNDED;
    }

    /**
     * 결제 취소 처리
     */
    public void cancel() {
        if (this.paymentStatus == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제는 취소할 수 없습니다. 환불을 사용하세요.");
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    /**
     * Payment 엔티티 팩토리 메서드
     */
    public static Payment createPayment(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        return Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(amount)
                .build();
    }
}