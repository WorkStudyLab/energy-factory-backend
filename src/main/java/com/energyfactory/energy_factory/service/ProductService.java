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

        return convertPageToListDto(productPage);
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
        return convertPageToListDto(productPage);
    }

    /**
     * 상품 검색 (상품명 기반)
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

        return convertPageToListDto(productPage);
    }

    /**
     * 모든 카테고리 목록 조회 (중복 제거, 정렬)
     * DB에 존재하는 실제 카테고리만 반환
     */
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    /**
     * Product 엔티티를 ProductResponseDto로 변환 (상세 조회용)
     * 태그, 영양성분(nutrition, vitamins), 목표별 점수를 포함한 전체 정보 반환
     */
    private ProductResponseDto convertToProductResponseDto(Product product) {
        // 태그 변환: ProductTag -> TagResponseDto
        List<ProductResponseDto.TagResponseDto> tags = product.getProductTags().stream()
                .map(productTag -> ProductResponseDto.TagResponseDto.builder()
                        .id(productTag.getTag().getId())
                        .name(productTag.getTag().getName())
                        .build())
                .collect(Collectors.toList());

        // 영양성분 변환: ProductNutrient -> NutritionDto, VitaminMineralDto, GoalScoresDto
        List<ProductNutrient> nutrients = product.getProductNutrients();
        NutritionDto nutrition = buildNutritionDto(nutrients);                      // 주요 영양소 (칼로리, 단백질 등)
        List<VitaminMineralDto> vitaminsAndMinerals = buildVitaminMinerals(nutrients);  // 비타민/미네랄
        GoalScoresDto goalScores = buildGoalScores(product);                        // 피트니스 목표별 점수

        // 이미지 목록 생성: imageUrl을 배열로 변환 (현재는 단일 이미지를 배열에 담음)
        List<String> images = buildImagesList(product.getImageUrl());

        // 배송 정보 생성
        ShippingInfoDto shipping = buildShippingInfo(product);

        // 상품 변형 리스트 생성
        List<ProductVariantDto> variants = buildVariants(product);

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .images(images)
                .brand(product.getBrand())
                .weight(product.getWeight())
                .description(product.getDescription())
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
                .originalPrice(product.getOriginalPrice())
                .discount(product.getDiscountRate())
                .shipping(shipping)
                .variants(variants)
                .build();
    }

    /**
     * Product 엔티티를 ProductSummaryDto로 변환 (목록 조회용)
     * 상품 리스트에 필요한 기본 정보만 포함 (영양성분 제외)
     */
    private ProductListResponseDto.ProductSummaryDto convertToProductSummaryDto(Product product) {
        // 태그명만 추출 (목록에서는 ID 불필요)
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
                .status(product.getStatus())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .tags(tagNames)
                .originalPrice(product.getOriginalPrice())
                .discount(product.getDiscountRate())
                .build();
    }

    /**
     * ProductNutrient 리스트에서 NutritionDto 생성
     * 주요 영양소(칼로리, 단백질, 탄수화물, 지방) 및 상세 영양소를 이름별로 매핑
     */
    private NutritionDto buildNutritionDto(List<ProductNutrient> nutrients) {
        NutritionDto.NutritionDtoBuilder builder = NutritionDto.builder();

        for (ProductNutrient n : nutrients) {
            String name = n.getName();
            String value = n.getValue();

            try {
                // 영양소 이름에 따라 적절한 필드에 값 설정
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
     * 주요 영양소 10개를 제외한 나머지 영양소 중 dailyPercentage가 있는 항목만 추출
     */
    private List<VitaminMineralDto> buildVitaminMinerals(List<ProductNutrient> nutrients) {
        // 주요 영양소 목록 (NutritionDto에 포함되는 항목들)
        List<String> mainNutrients = Arrays.asList(
                "칼로리", "단백질", "탄수화물", "지방",
                "포화지방", "트랜스지방", "콜레스테롤", "나트륨", "식이섬유", "당류"
        );

        return nutrients.stream()
                .filter(n -> !mainNutrients.contains(n.getName()))  // 주요 영양소 제외
                .filter(n -> n.getDailyPercentage() != null)        // dailyPercentage가 있는 것만 (비타민/미네랄 구분)
                .map(n -> VitaminMineralDto.builder()
                        .name(n.getName())
                        .amount(n.getValue() + n.getUnit())  // "0.5" + "mg" = "0.5mg"
                        .daily(n.getDailyPercentage())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Product에서 피트니스 목표별 점수 DTO 생성
     * 모든 점수가 null인 경우 null 반환 (JSON 응답에서 제외됨)
     */
    private GoalScoresDto buildGoalScores(Product product) {
        GoalScoresDto dto = GoalScoresDto.builder()
                .muscleGain(product.getScoreMuscleGain())
                .weightLoss(product.getScoreWeightLoss())
                .energy(product.getScoreEnergy())
                .recovery(product.getScoreRecovery())
                .health(product.getScoreHealth())
                .build();

        // 모든 점수가 null이면 null 반환 (상품에 점수 정보가 없는 경우)
        if (dto.getMuscleGain() == null && dto.getWeightLoss() == null
                && dto.getEnergy() == null && dto.getRecovery() == null
                && dto.getHealth() == null) {
            return null;
        }

        return dto;
    }

    /**
     * Page<Product>를 ProductListResponseDto로 변환 (공통 로직)
     * 상품 목록과 페이징 정보를 포함한 응답 DTO 생성
     */
    private ProductListResponseDto convertPageToListDto(Page<Product> productPage) {
        // 상품 엔티티 리스트를 요약 DTO로 변환
        List<ProductListResponseDto.ProductSummaryDto> products = productPage.getContent().stream()
                .map(this::convertToProductSummaryDto)
                .collect(Collectors.toList());

        // 페이징 정보 생성
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
     * 이미지 URL을 배열로 변환
     * 현재는 단일 이미지를 배열에 담아 반환 (향후 복수 이미지 지원 대비)
     *
     * @param imageUrl 단일 이미지 URL
     * @return 이미지 URL 리스트 (null 또는 빈 문자열인 경우 빈 리스트)
     */
    private List<String> buildImagesList(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return List.of();
        }
        return List.of(imageUrl);
    }

    /**
     * Product에서 배송 정보 DTO 생성
     * 배송 정보가 모두 null인 경우 null 반환
     *
     * @param product 상품 엔티티
     * @return 배송 정보 DTO (모든 필드가 null이면 null)
     */
    private ShippingInfoDto buildShippingInfo(Product product) {
        // 배송 정보가 하나라도 있으면 DTO 생성
        if (product.getShippingFee() != null ||
            product.getFreeShippingThreshold() != null ||
            product.getEstimatedDeliveryDays() != null) {

            return ShippingInfoDto.builder()
                    .fee(product.getShippingFee())
                    .freeShippingThreshold(product.getFreeShippingThreshold())
                    .estimatedDays(product.getEstimatedDeliveryDays())
                    .build();
        }

        return null;
    }

    /**
     * Product에서 상품 변형 리스트 생성
     * ProductVariant 엔티티를 ProductVariantDto로 변환
     *
     * @param product 상품 엔티티
     * @return 상품 변형 DTO 리스트 (변형이 없으면 빈 리스트)
     */
    private List<ProductVariantDto> buildVariants(Product product) {
        if (product.getProductVariants() == null || product.getProductVariants().isEmpty()) {
            return List.of();
        }

        return product.getProductVariants().stream()
                .map(variant -> ProductVariantDto.builder()
                        .id(variant.getId())
                        .name(variant.getVariantName())
                        .price(variant.getPrice())
                        .stock(variant.getStock())
                        .build())
                .collect(Collectors.toList());
    }
}