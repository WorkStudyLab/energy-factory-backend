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
 * 상품 변형(옵션) 엔티티
 * 같은 상품의 다양한 중량/용량 옵션을 관리
 * 예: 한우 등심 500g, 1kg, 2kg
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product_variant")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '상품 변형 고유 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_variant_product"))
    private Product product;

    @Column(name = "variant_name", nullable = false, columnDefinition = "VARCHAR(100) NOT NULL COMMENT '변형 이름(예: 500g, 1kg)'")
    private String variantName;

    @Column(name = "price", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) NOT NULL COMMENT '해당 변형의 가격'")
    private BigDecimal price;

    @Column(name = "stock", nullable = false, columnDefinition = "BIGINT NOT NULL DEFAULT 0 COMMENT '해당 변형의 재고'")
    @Builder.Default
    private Long stock = 0L;

    @Column(name = "is_default", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT false COMMENT '기본 옵션 여부'")
    @Builder.Default
    private Boolean isDefault = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '등록일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    // 재고 차감
    public void decreaseStock(Long quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stock);
        }
        this.stock -= quantity;
    }

    // 재고 증가 (취소 시 사용)
    public void increaseStock(Long quantity) {
        this.stock += quantity;
    }

    // 재고 확인
    public boolean hasStock(Long quantity) {
        return this.stock >= quantity;
    }
}
