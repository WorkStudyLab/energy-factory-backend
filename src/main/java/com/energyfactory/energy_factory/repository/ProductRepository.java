package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 상품 데이터 접근 레포지토리
 * 상품 조회, 검색, 필터링 기능을 위한 쿼리 메서드 제공
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 카테고리별 상품 조회
     */
    Page<Product> findByCategory(String category, Pageable pageable);
    
    /**
     * 상품명 키워드 검색
     */
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    /**
     * 가격 범위 조회
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * 판매 상태별 조회
     */
    Page<Product> findByStatus(String status, Pageable pageable);
    
    /**
     * 카테고리 + 키워드 복합 검색
     */
    Page<Product> findByCategoryAndNameContainingIgnoreCase(String category, String keyword, Pageable pageable);
    
    /**
     * 카테고리 + 가격 범위 조회
     */
    Page<Product> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * 복합 조건 검색 (카테고리, 키워드, 가격범위, 상태)
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Product> findByComplexConditions(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") String status,
            Pageable pageable
    );
    
    /**
     * 브랜드별 상품 조회
     */
    Page<Product> findByBrand(String brand, Pageable pageable);
    
    /**
     * 재고가 있는 상품만 조회
     */
    Page<Product> findByStockGreaterThan(Long minStock, Pageable pageable);
}