package com.energyfactory.energy_factory.entity;

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

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "order_items")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '주문 상세 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '주문 ID'")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '상품 ID'")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", columnDefinition = "BIGINT COMMENT '상품 변형 ID (옵션)'")
    private ProductVariant productVariant;

    @Column(name = "quantity", nullable = false, columnDefinition = "INT NOT NULL COMMENT '주문 수량'")
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) NOT NULL COMMENT '단가'")
    private BigDecimal price;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) NOT NULL COMMENT '상품별 총액'")
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    // 총액 계산
    public BigDecimal calculateTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // 주문 아이템 생성 팩토리 메서드 (Variant 포함)
    public static OrderItem of(Order order, Product product, ProductVariant variant, Integer quantity, BigDecimal price) {
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));

        return OrderItem.builder()
                .order(order)
                .product(product)
                .productVariant(variant)
                .quantity(quantity)
                .price(price)
                .totalPrice(totalPrice)
                .build();
    }

    // 주문 아이템 생성 팩토리 메서드 (Variant 없는 경우 - 하위 호환성)
    public static OrderItem of(Order order, Product product, Integer quantity, BigDecimal price) {
        return of(order, product, null, quantity, price);
    }
}
