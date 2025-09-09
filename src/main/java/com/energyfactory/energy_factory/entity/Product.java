package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;
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
    private Long stock;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50) NOT NULL COMMENT '판매 상태'")
    private String status;

    @Column(name = "storage", columnDefinition = "VARCHAR(100) COMMENT '보관 방법'")
    private String storage;

    @Column(name = "weight_unit", nullable = false, columnDefinition = "VARCHAR(10) NOT NULL COMMENT '중량 단위(g,ml,L 등)'")
    private String weightUnit;

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
}