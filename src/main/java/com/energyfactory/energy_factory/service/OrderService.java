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
    private final CartItemRepository cartItemRepository;
    private final NotificationService notificationService;

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

            // 판매 가능한 재고 확인 (총재고 - 예약재고)
            if (!variant.hasAvailableStock(itemDto.getQuantity().longValue())) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }

            // 가격 검증 (클라이언트에서 전달한 가격과 실제 Variant 가격 비교)
            if (variant.getPrice().compareTo(itemDto.getPrice()) != 0) {
                throw new BusinessException(ResultCode.INVALID_PRICE);
            }

            // 주문 항목 생성 (Variant 포함)
            OrderItem orderItem = OrderItem.of(order, product, variant, itemDto.getQuantity(), itemDto.getPrice());
            orderItems.add(orderItem);

            // 총액 누적
            totalPrice = totalPrice.add(orderItem.getTotalPrice());

            // 재고 예약 (결제 완료 시 확정됨)
            variant.reserveStock(itemDto.getQuantity().longValue());
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
            // 기본: PENDING 주문 제외 (결제 완료된 주문만 표시)
            orderPage = orderRepository.findByUserAndPaymentStatusNotOrderByCreatedAtDesc(user, PaymentStatus.PENDING, pageable);
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

        // 주문 상태에 따라 재고 처리
        PaymentStatus paymentStatus = order.getPaymentStatus();

        for (OrderItem orderItem : order.getOrderItems()) {
            ProductVariant variant = orderItem.getProductVariant();
            if (variant != null) {
                if (paymentStatus == PaymentStatus.PENDING) {
                    // 결제 전 취소: 예약만 해제
                    variant.releaseReservedStock(orderItem.getQuantity().longValue());
                } else if (paymentStatus == PaymentStatus.COMPLETED) {
                    // 결제 후 취소: 총재고 복원
                    variant.increaseStock(orderItem.getQuantity().longValue());
                }
                // FAILED, REFUNDED 등의 경우는 재고 처리 불필요
            }
        }

        // 주문 취소
        order.cancel();

        return convertToResponseDto(order);
    }

    @Transactional
    public OrderResponseDto cancelOrderByNumber(Long userId, Long orderNumber, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 주문이 해당 사용자의 것인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 취소 가능한 상태인지 확인
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ResultCode.CANNOT_CANCEL_ORDER);
        }

        // 주문 상태에 따라 재고 처리
        PaymentStatus paymentStatus = order.getPaymentStatus();

        for (OrderItem orderItem : order.getOrderItems()) {
            ProductVariant variant = orderItem.getProductVariant();
            if (variant != null) {
                if (paymentStatus == PaymentStatus.PENDING) {
                    // 결제 전 취소: 예약만 해제
                    variant.releaseReservedStock(orderItem.getQuantity().longValue());
                } else if (paymentStatus == PaymentStatus.COMPLETED) {
                    // 결제 후 취소: 총재고 복원
                    variant.increaseStock(orderItem.getQuantity().longValue());
                }
                // FAILED, REFUNDED 등의 경우는 재고 처리 불필요
            }
        }

        // 주문 취소
        order.cancel();

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

        // Payment 정보 변환 (가장 최신 payment 사용)
        OrderResponseDto.PaymentResponseDto paymentDto = null;
        if (!order.getPayments().isEmpty()) {
            Payment latestPayment = order.getPayments().get(order.getPayments().size() - 1);
            paymentDto = OrderResponseDto.PaymentResponseDto.builder()
                    .id(latestPayment.getId())
                    .paymentMethod(latestPayment.getPaymentMethod().name())
                    .paymentStatus(latestPayment.getPaymentStatus().name())
                    .transactionId(latestPayment.getTransactionId())
                    .amount(latestPayment.getAmount())
                    .paidAt(latestPayment.getPaidAt())
                    .build();
        }

        // 배송 정보 생성
        String fullAddress = order.getAddressLine1();
        if (order.getAddressLine2() != null && !order.getAddressLine2().isEmpty()) {
            fullAddress += " " + order.getAddressLine2();
        }

        OrderResponseDto.DeliveryInfoDto deliveryInfo = OrderResponseDto.DeliveryInfoDto.builder()
                .recipientName(order.getRecipientName())
                .address(fullAddress)
                .estimatedDeliveryDate(order.getCreatedAt().plusDays(2))
                .build();

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
                .payment(paymentDto)
                .deliveryInfo(deliveryInfo)
                .build();
    }

    private OrderListResponseDto.OrderSummaryDto convertToSummaryDto(Order order) {
        String representativeProductName = order.getOrderItems().isEmpty() ?
                "상품 없음" : order.getOrderItems().get(0).getProduct().getName();

        if (order.getOrderItems().size() > 1) {
            representativeProductName += " 외 " + (order.getOrderItems().size() - 1) + "개";
        }

        // Payment 정보 변환 (가장 최신 payment 사용)
        OrderListResponseDto.PaymentDto paymentDto = null;
        if (!order.getPayments().isEmpty()) {
            Payment latestPayment = order.getPayments().get(order.getPayments().size() - 1);
            paymentDto = OrderListResponseDto.PaymentDto.builder()
                    .id(latestPayment.getId())
                    .paymentMethod(latestPayment.getPaymentMethod().name())
                    .paymentStatus(latestPayment.getPaymentStatus().name())
                    .transactionId(latestPayment.getTransactionId())
                    .amount(latestPayment.getAmount())
                    .paidAt(latestPayment.getPaidAt())
                    .build();
        }

        // 배송 정보 생성
        String fullAddress = order.getAddressLine1();
        if (order.getAddressLine2() != null && !order.getAddressLine2().isEmpty()) {
            fullAddress += " " + order.getAddressLine2();
        }

        OrderListResponseDto.DeliveryDto deliveryDto = OrderListResponseDto.DeliveryDto.builder()
                .recipientName(order.getRecipientName())
                .address(fullAddress)
                .estimatedDeliveryDate(order.getCreatedAt().plusDays(2))
                .build();

        return OrderListResponseDto.OrderSummaryDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .recipientName(order.getRecipientName())
                .itemCount(order.getOrderItems().size())
                .representativeProductName(representativeProductName)
                .estimatedDeliveryDate(order.getCreatedAt().plusDays(2))
                .payment(paymentDto)
                .delivery(deliveryDto)
                .build();
    }

    /**
     * 주문 상태 변경 (관리자용)
     * 주문 상태가 변경되면 사용자에게 SSE 알림 전송
     *
     * @param orderId 주문 ID
     * @param newStatus 새로운 주문 상태
     * @return 변경된 주문 정보
     */
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 주문 상태 변경
        order.updateStatus(newStatus);

        // SSE 알림 전송
        notificationService.sendOrderNotification(
                order.getUser().getId(),
                order.getId(),
                order.getOrderNumber(),
                newStatus.name()
        );

        return convertToResponseDto(order);
    }

}