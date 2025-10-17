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
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '상품 고유 ID'")
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '상품명'")
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) NOT NULL COMMENT '상품 가격'")
    private BigDecimal price;

    @Column(name = "category", nullable = false, columnDefinition = "VARCHAR(100) NOT NULL COMMENT '카테고리(고기,채소,생선 등)'")
    private String category;

    @Column(name = "image_url", columnDefinition = "TEXT COMMENT '이미지 URL'")
    private String imageUrl;

    @Column(name = "brand", columnDefinition = "VARCHAR(100) COMMENT '브랜드명'")
    private String brand;

    @Column(name = "weight", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) COMMENT '판매 중량(숫자만)'")
    private BigDecimal weight;

    @Column(name = "description", columnDefinition = "TEXT COMMENT '상세 설명'")
    private String description;

    @Column(name = "stock", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '재고'")
    private Long stockQuantity;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50) NOT NULL COMMENT '판매 상태'")
    private String status;

    @Column(name = "storage", columnDefinition = "VARCHAR(100) COMMENT '보관 방법'")
    private String storage;

    @Column(name = "weight_unit", nullable = false, columnDefinition = "VARCHAR(10) NOT NULL COMMENT '중량 단위(g,ml,L 등)'")
    private String weightUnit;

    @Column(name = "average_rating", precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) DEFAULT 0.0 COMMENT '평균 별점 (0.0 ~ 5.0)'")
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", columnDefinition = "BIGINT DEFAULT 0 COMMENT '리뷰 개수'")
    @Builder.Default
    private Long reviewCount = 0L;

    // ===== 피트니스 목표별 점수 (0.0 ~ 5.0) =====
    @Column(name = "score_muscle_gain", precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) COMMENT '근육 증가 점수 (0.0-5.0)'")
    private BigDecimal scoreMuscleGain;

    @Column(name = "score_weight_loss", precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) COMMENT '체중 감량 점수 (0.0-5.0)'")
    private BigDecimal scoreWeightLoss;

    @Column(name = "score_energy", precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) COMMENT '에너지 향상 점수 (0.0-5.0)'")
    private BigDecimal scoreEnergy;

    @Column(name = "score_recovery", precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) COMMENT '회복 촉진 점수 (0.0-5.0)'")
    private BigDecimal scoreRecovery;

    @Column(name = "score_health", precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) COMMENT '전반적 건강 점수 (0.0-5.0)'")
    private BigDecimal scoreHealth;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '등록일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductTag> productTags = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductNutrient> productNutrients = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    // 재고 차감
    public void decreaseStock(Integer quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    // 재고 증가 (취소 시 사용)
    public void increaseStock(Integer quantity) {
        this.stockQuantity += quantity;
    }

    // 재고 확인
    public boolean hasStock(Integer quantity) {
        return this.stockQuantity >= quantity;
    }

    // 별점 업데이트 (새로운 리뷰가 추가될 때)
    public void updateRating(BigDecimal newRating) {
        if (newRating.compareTo(BigDecimal.ZERO) < 0 || newRating.compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("별점은 0.0에서 5.0 사이여야 합니다.");
        }
        
        BigDecimal totalRating = this.averageRating.multiply(new BigDecimal(this.reviewCount));
        totalRating = totalRating.add(newRating);
        this.reviewCount += 1;
        this.averageRating = totalRating.divide(new BigDecimal(this.reviewCount), 1, BigDecimal.ROUND_HALF_UP);
    }

    // 별점 재계산 (리뷰 삭제 시)
    public void removeRating(BigDecimal removedRating) {
        if (this.reviewCount <= 0) {
            throw new IllegalStateException("삭제할 리뷰가 없습니다.");
        }
        
        BigDecimal totalRating = this.averageRating.multiply(new BigDecimal(this.reviewCount));
        totalRating = totalRating.subtract(removedRating);
        this.reviewCount -= 1;
        
        if (this.reviewCount == 0) {
            this.averageRating = BigDecimal.ZERO;
        } else {
            this.averageRating = totalRating.divide(new BigDecimal(this.reviewCount), 1, BigDecimal.ROUND_HALF_UP);
        }
    }
}