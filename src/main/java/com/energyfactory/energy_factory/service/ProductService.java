package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.entity.Product;
import com.energyfactory.energy_factory.entity.ProductNutrient;
import com.energyfactory.energy_factory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
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
     * 통합 검색: 상품명, 태그명, 영양소명, 설명 검색
     */
    public ProductListResponseDto unifiedSearch(String keyword, String category, Pageable pageable) {
        Page<Product> productPage;
        
        if (category != null && !category.trim().isEmpty()) {
            // 카테고리 + 통합 검색
            productPage = productRepository.searchByKeywordAndCategory(keyword, category, pageable);
        } else {
            // 통합 검색만
            productPage = productRepository.searchByKeyword(keyword, pageable);
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
        List<ProductNutrient> nutrients = product.getProductNutrients();
        NutritionDto nutrition = buildNutritionDto(nutrients);
        List<VitaminMineralDto> vitaminsAndMinerals = buildVitaminMinerals(nutrients);
        GoalScoresDto goalScores = buildGoalScores(product);

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
                .nutrition(nutrition)
                .vitaminsAndMinerals(vitaminsAndMinerals)
                .goalScores(goalScores)
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

    /**
     * ProductNutrient 리스트에서 NutritionDto 생성
     */
    private NutritionDto buildNutritionDto(List<ProductNutrient> nutrients) {
        NutritionDto.NutritionDtoBuilder builder = NutritionDto.builder();

        for (ProductNutrient n : nutrients) {
            String name = n.getName();
            String value = n.getValue();

            try {
                switch (name) {
                    case "칼로리" -> builder.calories(Integer.parseInt(value));
                    case "단백질" -> builder.protein(new BigDecimal(value));
                    case "탄수화물" -> builder.carbs(new BigDecimal(value));
                    case "지방" -> builder.fat(new BigDecimal(value));
                    case "포화지방" -> builder.saturatedFat(new BigDecimal(value));
                    case "트랜스지방" -> builder.transFat(new BigDecimal(value));
                    case "콜레스테롤" -> builder.cholesterol(Integer.parseInt(value));
                    case "나트륨" -> builder.sodium(Integer.parseInt(value));
                    case "식이섬유" -> builder.fiber(new BigDecimal(value));
                    case "당류" -> builder.sugars(new BigDecimal(value));
                }
            } catch (NumberFormatException e) {
                // 변환 실패 시 무시 (해당 영양소는 null로 유지)
            }
        }

        return builder.build();
    }

    /**
     * ProductNutrient 리스트에서 비타민/미네랄 리스트 생성
     */
    private List<VitaminMineralDto> buildVitaminMinerals(List<ProductNutrient> nutrients) {
        // 주요 영양소 목록 (제외할 항목들)
        List<String> mainNutrients = Arrays.asList(
                "칼로리", "단백질", "탄수화물", "지방",
                "포화지방", "트랜스지방", "콜레스테롤", "나트륨", "식이섬유", "당류"
        );

        return nutrients.stream()
                .filter(n -> !mainNutrients.contains(n.getName()))  // 주요 영양소 제외
                .filter(n -> n.getDailyPercentage() != null)        // daily% 있는 것만
                .map(n -> VitaminMineralDto.builder()
                        .name(n.getName())
                        .amount(n.getValue() + n.getUnit())
                        .daily(n.getDailyPercentage())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Product에서 목표별 점수 DTO 생성
     */
    private GoalScoresDto buildGoalScores(Product product) {
        GoalScoresDto dto = GoalScoresDto.builder()
                .muscleGain(product.getScoreMuscleGain())
                .weightLoss(product.getScoreWeightLoss())
                .energy(product.getScoreEnergy())
                .recovery(product.getScoreRecovery())
                .health(product.getScoreHealth())
                .build();

        // 모든 점수가 null이면 null 반환
        if (dto.getMuscleGain() == null && dto.getWeightLoss() == null
                && dto.getEnergy() == null && dto.getRecovery() == null
                && dto.getHealth() == null) {
            return null;
        }

        return dto;
    }
}