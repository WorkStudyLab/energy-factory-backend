package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UQ_user_product",
            columnNames = {"user_id", "product_id"}
        )
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '리뷰 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_TO_review"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "FK_product_TO_review"))
    private Product product;

    @Column(name = "rating", nullable = false, precision = 2, scale = 1, columnDefinition = "DECIMAL(2,1) NOT NULL COMMENT '별점 (1.0 ~ 5.0, 0.5 단위)'")
    private BigDecimal rating;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    // 별점 유효성 검증
    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (rating == null) {
            throw new IllegalArgumentException("별점은 필수입니다.");
        }
        
        BigDecimal minRating = new BigDecimal("1.0");
        BigDecimal maxRating = new BigDecimal("5.0");
        
        if (rating.compareTo(minRating) < 0 || rating.compareTo(maxRating) > 0) {
            throw new IllegalArgumentException("별점은 1.0에서 5.0 사이여야 합니다.");
        }
        
        // 0.5 단위 검증
        BigDecimal doubled = rating.multiply(new BigDecimal("2"));
        if (doubled.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("별점은 0.5 단위로만 입력 가능합니다. (예: 1.0, 1.5, 2.0, ..., 5.0)");
        }
    }
}
