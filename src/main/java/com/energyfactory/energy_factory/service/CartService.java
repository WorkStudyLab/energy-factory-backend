package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.entity.*;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.*;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    // 무료배송 기준 금액 (설정 파일로 분리 가능)
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("3000");

    /**
     * 장바구니 조회
     */
    public CartListResponseDto getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByUserOrderByCreatedAtDesc(user);

        List<CartItemResponseDto> itemDtos = cartItems.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        // 총 금액 계산
        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 총 수량 계산
        Integer totalQuantity = cartItems.stream()
                .map(CartItem::getQuantity)
                .reduce(0, Integer::sum);

        // 배송비 계산
        BigDecimal shippingFee = totalPrice.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_FEE;

        // 무료배송까지 남은 금액
        BigDecimal amountToFreeShipping = FREE_SHIPPING_THRESHOLD.subtract(totalPrice);
        if (amountToFreeShipping.compareTo(BigDecimal.ZERO) < 0) {
            amountToFreeShipping = BigDecimal.ZERO;
        }

        // 최종 결제 금액
        BigDecimal finalPrice = totalPrice.add(shippingFee);

        // 영양소 합계 계산
        NutritionSummaryDto nutritionSummary = calculateNutritionSummary(cartItems);

        return CartListResponseDto.builder()
                .items(itemDtos)
                .itemCount(cartItems.size())
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .shippingFee(shippingFee)
                .freeShippingThreshold(FREE_SHIPPING_THRESHOLD)
                .amountToFreeShipping(amountToFreeShipping)
                .finalPrice(finalPrice)
                .nutritionSummary(nutritionSummary)
                .build();
    }

    /**
     * 장바구니 추가
     * 중복된 variant가 있으면 수량 증가
     */
    @Transactional
    public CartItemResponseDto addToCart(Long userId, CartItemAddRequestDto requestDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 2. 상품 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 3. Variant 조회
        ProductVariant variant = productVariantRepository.findById(requestDto.getVariantId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 4. Variant가 해당 상품의 것인지 검증
        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new BusinessException(ResultCode.INVALID_REQUEST);
        }

        // 5. 재고 확인
        if (!variant.hasStock(requestDto.getQuantity().longValue())) {
            throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
        }

        // 6. 중복 체크
        CartItem cartItem = cartItemRepository.findByUserAndProductVariant(user, variant)
                .orElse(null);

        if (cartItem != null) {
            // 이미 존재하면 수량 증가
            Integer newQuantity = cartItem.getQuantity() + requestDto.getQuantity();

            // 새로운 수량으로 재고 체크
            if (!variant.hasStock(newQuantity.longValue())) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }

            cartItem.updateQuantity(newQuantity);
        } else {
            // 새로 추가
            cartItem = CartItem.of(user, product, variant, requestDto.getQuantity());
            cartItem = cartItemRepository.save(cartItem);
        }

        return convertToResponseDto(cartItem);
    }

    /**
     * 장바구니 수량 변경
     */
    @Transactional
    public CartItemResponseDto updateCartItemQuantity(Long userId, Long cartItemId, CartItemUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByUserAndId(user, cartItemId)
                .orElseThrow(() -> new BusinessException(ResultCode.CART_ITEM_NOT_FOUND));

        // 재고 확인
        if (!cartItem.getProductVariant().hasStock(requestDto.getQuantity().longValue())) {
            throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
        }

        cartItem.updateQuantity(requestDto.getQuantity());

        return convertToResponseDto(cartItem);
    }

    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByUserAndId(user, cartItemId)
                .orElseThrow(() -> new BusinessException(ResultCode.CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);
    }

    /**
     * 장바구니 전체 삭제
     */
    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        cartItemRepository.deleteByUser(user);
    }

    /**
     * 장바구니 선택 삭제
     */
    @Transactional
    public void deleteSelectedCartItems(Long userId, List<Long> cartItemIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        cartItemRepository.deleteByUserAndIdIn(user, cartItemIds);
    }

    /**
     * CartItem을 ResponseDto로 변환
     */
    private CartItemResponseDto convertToResponseDto(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();
        Product product = cartItem.getProduct();

        // 영양소 정보 생성
        NutritionDto nutrition = buildNutritionDto(product.getProductNutrients());

        return CartItemResponseDto.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .productCategory(product.getCategory())
                .variantId(variant.getId())
                .variantName(variant.getVariantName())
                .price(variant.getPrice())
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .stock(variant.getStock())
                .isAvailable(cartItem.isAvailable())
                .productStatus(product.getStatus())
                .nutrition(nutrition)
                .weight(product.getWeight())
                .weightUnit(product.getWeightUnit())
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
                // 변환 실패 시 무시
            }
        }

        return builder.build();
    }

    /**
     * 장바구니 전체 영양소 합계 및 칼로리 비율 계산
     *
     * 영양소별 칼로리 환산:
     * - 단백질: 1g = 4kcal
     * - 탄수화물: 1g = 4kcal
     * - 지방: 1g = 9kcal
     */
    private NutritionSummaryDto calculateNutritionSummary(List<CartItem> cartItems) {
        BigDecimal totalProteinGrams = BigDecimal.ZERO;
        BigDecimal totalCarbsGrams = BigDecimal.ZERO;
        BigDecimal totalFatGrams = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer quantity = cartItem.getQuantity();
            BigDecimal weight = product.getWeight() != null ? product.getWeight() : BigDecimal.valueOf(100);

            // 영양소는 100g 기준이므로, 실제 중량에 맞게 계산
            // (상품 중량 / 100g) * 수량
            BigDecimal multiplier = weight.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(quantity));

            NutritionDto nutrition = buildNutritionDto(product.getProductNutrients());

            if (nutrition.getProtein() != null) {
                totalProteinGrams = totalProteinGrams.add(nutrition.getProtein().multiply(multiplier));
            }
            if (nutrition.getCarbs() != null) {
                totalCarbsGrams = totalCarbsGrams.add(nutrition.getCarbs().multiply(multiplier));
            }
            if (nutrition.getFat() != null) {
                totalFatGrams = totalFatGrams.add(nutrition.getFat().multiply(multiplier));
            }
        }

        // 영양소별 칼로리 계산
        BigDecimal proteinCalories = totalProteinGrams.multiply(BigDecimal.valueOf(4)); // 단백질 1g = 4kcal
        BigDecimal carbsCalories = totalCarbsGrams.multiply(BigDecimal.valueOf(4));     // 탄수화물 1g = 4kcal
        BigDecimal fatCalories = totalFatGrams.multiply(BigDecimal.valueOf(9));         // 지방 1g = 9kcal

        // 총 칼로리
        BigDecimal totalCalories = proteinCalories.add(carbsCalories).add(fatCalories);

        // 칼로리 비율 계산 (%)
        BigDecimal proteinRatio = BigDecimal.ZERO;
        BigDecimal carbsRatio = BigDecimal.ZERO;
        BigDecimal fatRatio = BigDecimal.ZERO;

        if (totalCalories.compareTo(BigDecimal.ZERO) > 0) {
            proteinRatio = proteinCalories.divide(totalCalories, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            carbsRatio = carbsCalories.divide(totalCalories, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            fatRatio = fatCalories.divide(totalCalories, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return NutritionSummaryDto.builder()
                .totalCalories(totalCalories.setScale(1, RoundingMode.HALF_UP))
                .totalProtein(totalProteinGrams.setScale(1, RoundingMode.HALF_UP))
                .totalCarbs(totalCarbsGrams.setScale(1, RoundingMode.HALF_UP))
                .totalFat(totalFatGrams.setScale(1, RoundingMode.HALF_UP))
                .proteinRatio(proteinRatio.setScale(1, RoundingMode.HALF_UP))
                .carbsRatio(carbsRatio.setScale(1, RoundingMode.HALF_UP))
                .fatRatio(fatRatio.setScale(1, RoundingMode.HALF_UP))
                .build();
    }
}
