package com.energyfactory.energy_factory.entity;

import com.energyfactory.energy_factory.utils.enums.OrderStatus;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '주문 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT 'FK 주문자 아이디'")
    private User user;

    @Column(name = "order_number", unique = true, nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '주문번호'")
    private Long orderNumber;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) NOT NULL COMMENT '주문 총 합계'")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '주문 상태(배송 상태)'")
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "payment_status", nullable = false, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '결제 상태(완료,취소,환불)'")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "recipient_name", nullable = false, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '수령인'")
    private String recipientName;

    @Column(name = "phone_number", nullable = false, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '수령인 전화번호'")
    private String phoneNumber;

    @Column(name = "postal_code", nullable = false, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '우편번호'")
    private String postalCode;

    @Column(name = "address_line1", nullable = false, columnDefinition = "TEXT NOT NULL COMMENT '기본주소'")
    private String addressLine1;

    @Column(name = "address_line2", columnDefinition = "TEXT COMMENT '상세주소'")
    private String addressLine2;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    // 주문번호 생성 (시간 기반)
    public static Long generateOrderNumber() {
        return System.currentTimeMillis();
    }

    // 주문 상태 변경
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    // 결제 상태 변경
    public void updatePaymentStatus(PaymentStatus newPaymentStatus) {
        this.paymentStatus = newPaymentStatus;
    }

    // 주문 취소
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    // 주문 총액 계산
    public BigDecimal calculateTotalPrice() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}