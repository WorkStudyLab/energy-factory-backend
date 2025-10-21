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

/**
 * 장바구니 아이템 엔티티
 * 사용자의 장바구니에 담긴 상품(variant) 정보를 저장
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "cart_items",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_user_variant", columnNames = {"user_id", "variant_id"})
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '장바구니 아이템 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '사용자 ID'")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '상품 ID'")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '상품 변형 ID'")
    private ProductVariant productVariant;

    @Column(name = "quantity", nullable = false, columnDefinition = "INT NOT NULL COMMENT '수량'")
    private Integer quantity;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    // 수량 증가
    public void increaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가 수량은 0보다 커야 합니다.");
        }
        this.quantity += amount;
    }

    // 수량 변경
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        this.quantity = newQuantity;
    }

    // 총 금액 계산
    public BigDecimal getTotalPrice() {
        return productVariant.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // 재고 확인
    public boolean isAvailable() {
        return productVariant.hasStock(quantity.longValue());
    }

    // 팩토리 메서드
    public static CartItem of(User user, Product product, ProductVariant variant, Integer quantity) {
        return CartItem.builder()
                .user(user)
                .product(product)
                .productVariant(variant)
                .quantity(quantity)
                .build();
    }
}
