package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * 상품 ID로 모든 변형 조회
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * 기본 변형 조회
     */
    ProductVariant findByProductIdAndIsDefaultTrue(Long productId);
}
