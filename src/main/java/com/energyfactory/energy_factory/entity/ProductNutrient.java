package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product_nutrients")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductNutrient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '상품 영양성분 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '상품 고유 ID'")
    private Product product;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(100) NOT NULL COMMENT '영양소 이름(칼로리,탄단지)'")
    private String name;

    @Column(name = "value", nullable = false, columnDefinition = "VARCHAR(50) NOT NULL COMMENT '영양소 값'")
    private String value;

    @Column(name = "unit", nullable = false, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '단위(g,kcal)'")
    private String unit;

    @Column(name = "daily_percentage", columnDefinition = "INT COMMENT '일일 권장 섭취량 대비 % (0-100)'")
    private Integer dailyPercentage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '등록일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;


}