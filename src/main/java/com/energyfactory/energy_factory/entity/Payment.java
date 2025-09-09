package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '결제 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '주문 ID'")
    private Order order;

    @Column(name = "payment_method", nullable = false, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '결제 수단'")
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '결제 상태(결제,환불)'")
    private String paymentStatus;

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


}