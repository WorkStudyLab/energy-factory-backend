package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.entity.*;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.*;
import com.energyfactory.energy_factory.utils.enums.OrderStatus;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public OrderResponseDto createOrder(Long userId, OrderCreateRequestDto requestDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 2. 주문 생성
        Order order = Order.builder()
                .user(user)
                .orderNumber(Order.generateOrderNumber())
                .recipientName(requestDto.getRecipientName())
                .phoneNumber(requestDto.getPhoneNumber())
                .postalCode(requestDto.getPostalCode())
                .addressLine1(requestDto.getAddressLine1())
                .addressLine2(requestDto.getAddressLine2())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // 3. 주문 항목 생성 및 재고 확인
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderCreateRequestDto.OrderItemCreateDto itemDto : requestDto.getOrderItems()) {
            // 상품 조회
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

            // Variant 조회 (없으면 기본 Variant 사용)
            ProductVariant variant = null;
            if (itemDto.getVariantId() != null) {
                variant = productVariantRepository.findById(itemDto.getVariantId())
                        .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

                // 선택한 Variant가 해당 상품의 것인지 검증
                if (!variant.getProduct().getId().equals(product.getId())) {
                    throw new BusinessException(ResultCode.INVALID_REQUEST);
                }
            } else {
                // variantId가 없으면 기본 Variant 조회
                variant = productVariantRepository.findByProductIdAndIsDefaultTrue(product.getId());
                if (variant == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND);
                }
            }

            // 재고 확인
            if (!variant.hasStock(itemDto.getQuantity().longValue())) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }

            // 가격 검증 (클라이언트에서 전달한 가격과 실제 Variant 가격 비교)
            if (!variant.getPrice().equals(itemDto.getPrice())) {
                throw new BusinessException(ResultCode.INVALID_PRICE);
            }

            // 주문 항목 생성 (Variant 포함)
            OrderItem orderItem = OrderItem.of(order, product, variant, itemDto.getQuantity(), itemDto.getPrice());
            orderItems.add(orderItem);

            // 총액 누적
            totalPrice = totalPrice.add(orderItem.getTotalPrice());

            // 재고 차감
            variant.decreaseStock(itemDto.getQuantity().longValue());
        }

        // 4. 총액 설정 및 주문 저장
        Order savedOrder = Order.builder()
                .user(order.getUser())
                .orderNumber(order.getOrderNumber())
                .totalPrice(totalPrice)
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .recipientName(order.getRecipientName())
                .phoneNumber(order.getPhoneNumber())
                .postalCode(order.getPostalCode())
                .addressLine1(order.getAddressLine1())
                .addressLine2(order.getAddressLine2())
                .build();
        savedOrder = orderRepository.save(savedOrder);

        // 5. 주문 항목들을 주문에 추가하고 저장
        for (OrderItem orderItem : orderItems) {
            OrderItem savedOrderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(orderItem.getProduct())
                    .productVariant(orderItem.getProductVariant())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .totalPrice(orderItem.getTotalPrice())
                    .build();
            savedOrder.getOrderItems().add(savedOrderItem);
        }
        orderItemRepository.saveAll(savedOrder.getOrderItems());

        return convertToResponseDto(savedOrder);
    }

    public OrderListResponseDto getOrders(Long userId, String status, String paymentStatus, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Page<Order> orderPage;

        // 필터링 조건에 따른 조회
        if (status != null && paymentStatus != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            PaymentStatus payStatus = PaymentStatus.valueOf(paymentStatus.toUpperCase());
            orderPage = orderRepository.findByUserAndStatusAndPaymentStatusOrderByCreatedAtDesc(
                    user, orderStatus, payStatus, pageable);
        } else if (status != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orderPage = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, orderStatus, pageable);
        } else if (paymentStatus != null) {
            PaymentStatus payStatus = PaymentStatus.valueOf(paymentStatus.toUpperCase());
            orderPage = orderRepository.findByUserAndPaymentStatusOrderByCreatedAtDesc(user, payStatus, pageable);
        } else {
            orderPage = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        List<OrderListResponseDto.OrderSummaryDto> orders = orderPage.getContent().stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());

        OrderListResponseDto.PageInfoDto pageInfo = OrderListResponseDto.PageInfoDto.builder()
                .currentPage(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .build();

        return OrderListResponseDto.builder()
                .orders(orders)
                .pageInfo(pageInfo)
                .build();
    }

    public OrderResponseDto getOrder(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 주문이 해당 사용자의 것인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        return convertToResponseDto(order);
    }

    public OrderResponseDto getOrderByNumber(Long userId, Long orderNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 주문이 해당 사용자의 것인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        return convertToResponseDto(order);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long userId, Long orderId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 주문이 해당 사용자의 것인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 취소 가능한 상태인지 확인
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ResultCode.CANNOT_CANCEL_ORDER);
        }

        // 주문 취소
        order.cancel();

        // 재고 복원
        for (OrderItem orderItem : order.getOrderItems()) {
            ProductVariant variant = orderItem.getProductVariant();
            if (variant != null) {
                variant.increaseStock(orderItem.getQuantity().longValue());
            }
        }

        return convertToResponseDto(order);
    }

    private OrderResponseDto convertToResponseDto(Order order) {
        List<OrderResponseDto.OrderItemResponseDto> orderItemDtos = order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderResponseDto.OrderItemResponseDto.OrderItemResponseDtoBuilder builder =
                            OrderResponseDto.OrderItemResponseDto.builder()
                                    .id(orderItem.getId())
                                    .productId(orderItem.getProduct().getId())
                                    .productName(orderItem.getProduct().getName())
                                    .quantity(orderItem.getQuantity())
                                    .price(orderItem.getPrice())
                                    .totalPrice(orderItem.getTotalPrice());

                    // Variant 정보 추가
                    if (orderItem.getProductVariant() != null) {
                        builder.variantId(orderItem.getProductVariant().getId())
                               .variantName(orderItem.getProductVariant().getVariantName());
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .recipientName(order.getRecipientName())
                .phoneNumber(order.getPhoneNumber())
                .postalCode(order.getPostalCode())
                .addressLine1(order.getAddressLine1())
                .addressLine2(order.getAddressLine2())
                .orderItems(orderItemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderListResponseDto.OrderSummaryDto convertToSummaryDto(Order order) {
        String representativeProductName = order.getOrderItems().isEmpty() ? 
                "상품 없음" : order.getOrderItems().get(0).getProduct().getName();
        
        if (order.getOrderItems().size() > 1) {
            representativeProductName += " 외 " + (order.getOrderItems().size() - 1) + "개";
        }

        return OrderListResponseDto.OrderSummaryDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .recipientName(order.getRecipientName())
                .itemCount(order.getOrderItems().size())
                .representativeProductName(representativeProductName)
                .createdAt(order.getCreatedAt())
                .build();
    }
}