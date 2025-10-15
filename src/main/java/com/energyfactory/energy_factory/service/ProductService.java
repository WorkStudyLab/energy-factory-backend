package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.ProductListResponseDto;
import com.energyfactory.energy_factory.dto.ProductResponseDto;
import com.energyfactory.energy_factory.entity.Product;
import com.energyfactory.energy_factory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 비즈니스 로직 서비스
 * 상품 조회, 검색, 필터링 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 목록 조회 (필터링, 페이징, 정렬 지원)
     */
    public ProductListResponseDto getProducts(
            String category,
            String keyword,
            String status,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        // 가격 범위 변환
        BigDecimal minPriceBd = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceBd = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        
        // 복합 조건 검색
        Page<Product> productPage = productRepository.findByComplexConditions(
                category, keyword, minPriceBd, maxPriceBd, status, pageable
        );
        
        // DTO 변환
        List<ProductListResponseDto.ProductSummaryDto> products = productPage.getContent().stream()
                .map(this::convertToProductSummaryDto)
                .collect(Collectors.toList());
        
        // 페이지 정보 생성
        ProductListResponseDto.PageInfoDto pageInfo = ProductListResponseDto.PageInfoDto.builder()
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
        
        return ProductListResponseDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * 상품 상세 조회
     */
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + id));
        
        return convertToProductResponseDto(product);
    }

    /**
     * 카테고리별 상품 조회
     */
    public ProductListResponseDto getProductsByCategory(String category, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        
        List<ProductListResponseDto.ProductSummaryDto> products = productPage.getContent().stream()
                .map(this::convertToProductSummaryDto)
                .collect(Collectors.toList());
        
        ProductListResponseDto.PageInfoDto pageInfo = ProductListResponseDto.PageInfoDto.builder()
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
        
        return ProductListResponseDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * 상품 검색
     */
    public ProductListResponseDto searchProducts(String keyword, String category, Pageable pageable) {
        Page<Product> productPage;
        
        if (category != null && !category.trim().isEmpty()) {
            // 카테고리 + 키워드 검색
            productPage = productRepository.findByCategoryAndNameContainingIgnoreCase(
                    category, keyword, pageable
            );
        } else {
            // 키워드만 검색
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        
        List<ProductListResponseDto.ProductSummaryDto> products = productPage.getContent().stream()
                .map(this::convertToProductSummaryDto)
                .collect(Collectors.toList());
        
        ProductListResponseDto.PageInfoDto pageInfo = ProductListResponseDto.PageInfoDto.builder()
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
        
        return ProductListResponseDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * Product 엔티티를 ProductResponseDto로 변환
     */
    private ProductResponseDto convertToProductResponseDto(Product product) {
        // 태그 변환
        List<ProductResponseDto.TagResponseDto> tags = product.getProductTags().stream()
                .map(productTag -> ProductResponseDto.TagResponseDto.builder()
                        .id(productTag.getTag().getId())
                        .name(productTag.getTag().getName())
                        .build())
                .collect(Collectors.toList());
        
        // 영양성분 변환
        List<ProductResponseDto.NutrientResponseDto> nutrients = product.getProductNutrients().stream()
                .map(nutrient -> ProductResponseDto.NutrientResponseDto.builder()
                        .id(nutrient.getId())
                        .name(nutrient.getName())
                        .value(nutrient.getValue())
                        .unit(nutrient.getUnit())
                        .build())
                .collect(Collectors.toList());
        
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .brand(product.getBrand())
                .weight(product.getWeight())
                .description(product.getDescription())
                .stock(product.getStockQuantity())
                .status(product.getStatus())
                .storage(product.getStorage())
                .weightUnit(product.getWeightUnit())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .tags(tags)
                .nutrients(nutrients)
                .build();
    }

    /**
     * Product 엔티티를 ProductSummaryDto로 변환 (목록용)
     */
    private ProductListResponseDto.ProductSummaryDto convertToProductSummaryDto(Product product) {
        // 태그명 리스트 추출
        List<String> tagNames = product.getProductTags().stream()
                .map(productTag -> productTag.getTag().getName())
                .collect(Collectors.toList());
        
        return ProductListResponseDto.ProductSummaryDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .brand(product.getBrand())
                .weight(product.getWeight())
                .weightUnit(product.getWeightUnit())
                .stock(product.getStockQuantity())
                .status(product.getStatus())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .tags(tagNames)
                .build();
    }
}