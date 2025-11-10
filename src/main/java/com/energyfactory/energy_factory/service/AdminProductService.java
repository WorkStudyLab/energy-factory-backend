package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.ProductCreateRequestDto;
import com.energyfactory.energy_factory.dto.ProductResponseDto;
import com.energyfactory.energy_factory.dto.ProductUpdateRequestDto;
import com.energyfactory.energy_factory.entity.Product;
import com.energyfactory.energy_factory.entity.ProductTag;
import com.energyfactory.energy_factory.entity.Tag;
import com.energyfactory.energy_factory.repository.OrderItemRepository;
import com.energyfactory.energy_factory.repository.ProductRepository;
import com.energyfactory.energy_factory.repository.ProductTagRepository;
import com.energyfactory.energy_factory.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    /**
     * 상품 생성
     */
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        // Product 엔티티 생성
        Product product = Product.builder()
                .name(requestDto.getName())
                .category(requestDto.getCategory())
                .price(requestDto.getPrice())
                .brand(requestDto.getBrand())
                .originalPrice(requestDto.getOriginalPrice())
                .discountRate(requestDto.getDiscount())
                .weight(requestDto.getWeight())
                .weightUnit(requestDto.getWeightUnit())
                .status(requestDto.getStatus() != null ? requestDto.getStatus() : "AVAILABLE")
                .imageUrl(requestDto.getImageUrl())
                .description(requestDto.getDescription())
                .storage(requestDto.getStorage())
                .shippingFee(requestDto.getShippingFee() != null ? requestDto.getShippingFee() : BigDecimal.ZERO)
                .freeShippingThreshold(requestDto.getFreeShippingThreshold())
                .estimatedDeliveryDays(requestDto.getEstimatedDeliveryDays())
                .scoreMuscleGain(requestDto.getScoreMuscleGain())
                .scoreWeightLoss(requestDto.getScoreWeightLoss())
                .scoreEnergy(requestDto.getScoreEnergy())
                .scoreRecovery(requestDto.getScoreRecovery())
                .scoreHealth(requestDto.getScoreHealth())
                .build();

        // 상품 저장
        Product savedProduct = productRepository.save(product);

        // 태그 처리
        if (requestDto.getTags() != null && !requestDto.getTags().isEmpty()) {
            processTags(savedProduct, requestDto.getTags());
        }

        // DTO 변환 및 반환
        return productService.getProductById(savedProduct.getId());
    }

    /**
     * 상품 수정
     */
    public ProductResponseDto updateProduct(Long id, ProductUpdateRequestDto requestDto) {
        // 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + id));

        // 상품 정보 업데이트
        product.update(
                requestDto.getName(),
                requestDto.getCategory(),
                requestDto.getPrice(),
                requestDto.getBrand(),
                requestDto.getOriginalPrice(),
                requestDto.getDiscount(),
                requestDto.getWeight(),
                requestDto.getWeightUnit(),
                requestDto.getStatus(),
                requestDto.getImageUrl(),
                requestDto.getDescription(),
                requestDto.getStorage(),
                requestDto.getShippingFee(),
                requestDto.getFreeShippingThreshold(),
                requestDto.getEstimatedDeliveryDays(),
                requestDto.getScoreMuscleGain(),
                requestDto.getScoreWeightLoss(),
                requestDto.getScoreEnergy(),
                requestDto.getScoreRecovery(),
                requestDto.getScoreHealth()
        );

        // 태그 처리
        if (requestDto.getTags() != null) {
            // 기존 태그 삭제
            productTagRepository.deleteByProduct(product);
            // 새 태그 추가
            if (!requestDto.getTags().isEmpty()) {
                processTags(product, requestDto.getTags());
            }
        }

        // DTO 변환 및 반환
        return productService.getProductById(product.getId());
    }

    /**
     * 상품 삭제
     */
    public void deleteProduct(Long id) {
        // 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + id));

        // 주문 내역 확인
        Long orderCount = orderItemRepository.getTotalQuantityByProduct(product);
        if (orderCount != null && orderCount > 0) {
            throw new RuntimeException("주문 내역이 있는 상품은 삭제할 수 없습니다. (주문 수량: " + orderCount + ")");
        }

        // 상품 삭제
        productRepository.delete(product);
    }

    /**
     * 태그 처리 헬퍼 메서드
     */
    private void processTags(Product product, List<String> tagNames) {
        for (String tagName : tagNames) {
            // 태그 조회 또는 생성
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder()
                                    .name(tagName)
                                    .build()
                    ));

            // ProductTag 연결 (중복 체크)
            if (!productTagRepository.existsByProductAndTag(product, tag)) {
                ProductTag productTag = ProductTag.builder()
                        .product(product)
                        .tag(tag)
                        .build();
                productTagRepository.save(productTag);
            }
        }
    }
}
