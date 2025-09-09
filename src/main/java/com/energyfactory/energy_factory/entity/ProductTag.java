package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_tags",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "tag_id"})
        }
)
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '상품 태그 ID'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '상품 ID'")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '태그 ID'")
    private Tag tag;

}