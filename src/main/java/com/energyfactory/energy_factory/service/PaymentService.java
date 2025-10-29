package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.client.TossPaymentsClient;
import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.entity.CartItem;
import com.energyfactory.energy_factory.entity.Order;
import com.energyfactory.energy_factory.entity.Payment;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.CartItemRepository;
import com.energyfactory.energy_factory.repository.OrderRepository;
import com.energyfactory.energy_factory.repository.PaymentRepository;
import com.energyfactory.energy_factory.utils.enums.OrderStatus;
import com.energyfactory.energy_factory.utils.enums.PaymentMethod;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final CartItemRepository cartItemRepository;
    private final Random random = new Random();

    /**
     * Mock 결제 처리
     * 실제 PG사 연동 전까지 가상 결제 시스템으로 동작
     */
    @Transactional
    public PaymentResponseDto processPayment(PaymentCreateRequestDto requestDto) {
        // 1. 주문 조회 및 검증
        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 2. 결제 금액 검증 (주문 총액과 일치해야 함)
        if (order.getTotalPrice().compareTo(requestDto.getAmount()) != 0) {
            throw new BusinessException(ResultCode.INVALID_PRICE);
        }

        // 3. 결제 생성
        Payment payment = Payment.createPayment(order, requestDto.getPaymentMethod(), requestDto.getAmount());
        Payment savedPayment = paymentRepository.save(payment);

        // 4. Mock PG 결제 처리 (90% 성공률)
        boolean paymentSuccess = mockPaymentGateway(requestDto.getAmount());

        if (paymentSuccess) {
            // 결제 성공 처리
            savedPayment.completePayment();
            
            // 주문 상태를 결제 완료로 변경
            order.updatePaymentStatus(PaymentStatus.COMPLETED);
            
        } else {
            // 결제 실패 처리
            savedPayment.failPayment();
        }

        return convertToResponseDto(savedPayment);
    }

    /**
     * 주문별 결제 정보 조회
     */
    public PaymentResponseDto getPaymentByOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        return convertToResponseDto(payment);
    }

    /**
     * 결제 정보 조회
     */
    public PaymentResponseDto getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        return convertToResponseDto(payment);
    }

    /**
     * 결제 환불 처리
     */
    @Transactional
    public PaymentResponseDto refundPayment(Long paymentId, String reason) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 2. 환불 가능 상태 확인
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ResultCode.INVALID_REQUEST);
        }

        // 3. Mock 환불 처리 (항상 성공)
        boolean refundSuccess = mockRefundGateway(payment.getTransactionId(), payment.getAmount());

        if (refundSuccess) {
            // 환불 처리
            payment.refund();
            
            // 주문 상태를 환불로 변경
            Order order = payment.getOrder();
            order.updatePaymentStatus(PaymentStatus.REFUNDED);
            order.updateStatus(OrderStatus.CANCELLED);

            // 재고 복원
            order.getOrderItems().forEach(orderItem -> {
                if (orderItem.getProductVariant() != null) {
                    orderItem.getProductVariant().increaseStock(orderItem.getQuantity().longValue());
                }
            });
        }

        return convertToResponseDto(payment);
    }

    /**
     * Mock PG 결제 처리 (가상 결제 게이트웨이)
     * 실제 PG사 연동 시 이 메서드를 실제 API 호출로 대체
     */
    private boolean mockPaymentGateway(BigDecimal amount) {
        try {
            // 결제 처리 시뮬레이션 (500ms ~ 2초)
            Thread.sleep(500 + random.nextInt(1500));
            
            // 90% 성공률 시뮬레이션
            return random.nextInt(100) < 90;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Mock 환불 처리 (가상 환불 게이트웨이)
     */
    private boolean mockRefundGateway(String transactionId, BigDecimal amount) {
        try {
            // 환불 처리 시뮬레이션 (300ms ~ 1초)
            Thread.sleep(300 + random.nextInt(700));
            
            // 환불은 항상 성공
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Payment 엔티티를 PaymentResponseDto로 변환
     */
    private PaymentResponseDto convertToResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentStatus(payment.getPaymentStatus().name())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    // ==================== 토스페이먼츠 연동 메서드 ====================

    /**
     * 토스페이먼츠 결제 승인 처리
     * 프론트엔드에서 받은 paymentKey, orderId, amount로 실제 결제 승인
     *
     * @param confirmRequest 결제 승인 요청 정보
     * @return 결제 응답 DTO
     */
    @Transactional
    public PaymentResponseDto confirmTossPayment(TossPaymentConfirmRequestDto confirmRequest) {
        log.info("토스페이먼츠 결제 승인 시작 - orderId: {}, amount: {}",
                confirmRequest.getOrderId(), confirmRequest.getAmount());

        // 1. orderId에서 실제 주문 ID 추출
        // 형식: ORDER_{실제ID}_{타임스탬프} 또는 숫자만
        Long orderId = extractOrderId(confirmRequest.getOrderId());
        log.info("추출된 주문 ID: {}", orderId);

        // 2. 주문 조회 및 검증
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 2. 결제 금액 검증 (주문 총액과 일치해야 함)
        // 소수점이 있을 수 있으므로 반올림해서 비교 (원화는 정수만 사용)
        BigDecimal orderAmount = order.getTotalPrice().setScale(0, java.math.RoundingMode.HALF_UP);
        BigDecimal requestAmount = confirmRequest.getAmount().setScale(0, java.math.RoundingMode.HALF_UP);

        if (orderAmount.compareTo(requestAmount) != 0) {
            log.error("결제 금액 불일치 - 주문 금액: {} (반올림: {}), 결제 요청 금액: {} (반올림: {})",
                    order.getTotalPrice(), orderAmount, confirmRequest.getAmount(), requestAmount);
            throw new BusinessException(ResultCode.INVALID_PRICE);
        }

        // 3. 토스페이먼츠 결제 승인 API 호출
        TossPaymentResponseDto tossResponse = tossPaymentsClient.confirmPayment(confirmRequest);

        // 4. 결제 정보 DB 저장
        PaymentMethod paymentMethod = convertTossMethodToEnum(tossResponse.getMethod());
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(tossResponse.getTotalAmount())
                .transactionId(tossResponse.getPaymentKey())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 5. 결제 상태에 따른 처리
        if ("DONE".equals(tossResponse.getStatus())) {
            savedPayment.completePayment();
            order.updatePaymentStatus(PaymentStatus.COMPLETED);

            // 결제 완료 시 재고 차감
            order.getOrderItems().forEach(orderItem -> {
                if (orderItem.getProductVariant() != null) {
                    orderItem.getProductVariant().decreaseStock(orderItem.getQuantity().longValue());
                    log.info("재고 차감 - 상품변형 ID: {}, 수량: {}",
                            orderItem.getProductVariant().getId(), orderItem.getQuantity());
                }
            });

            // 결제 완료 시 장바구니에서 해당 상품들 삭제
            deleteCartItemsForCompletedOrder(order);

            log.info("토스페이먼츠 결제 승인 완료 - paymentKey: {}", tossResponse.getPaymentKey());
        } else {
            savedPayment.failPayment();
            log.error("토스페이먼츠 결제 상태 이상 - status: {}", tossResponse.getStatus());
        }

        return convertToResponseDto(savedPayment);
    }

    /**
     * 토스페이먼츠 결제 취소 처리
     *
     * @param paymentId 결제 ID
     * @param reason 취소 사유
     * @return 결제 응답 DTO
     */
    @Transactional
    public PaymentResponseDto cancelTossPayment(Long paymentId, String reason) {
        log.info("토스페이먼츠 결제 취소 시작 - paymentId: {}, reason: {}", paymentId, reason);

        // 1. 결제 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        // 2. 환불 가능 상태 확인
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ResultCode.INVALID_REQUEST);
        }

        // 3. 토스페이먼츠 취소 API 호출
        TossPaymentCancelRequestDto cancelRequest = TossPaymentCancelRequestDto.builder()
                .cancelReason(reason)
                .build();

        TossPaymentResponseDto tossResponse = tossPaymentsClient.cancelPayment(
                payment.getTransactionId(),
                cancelRequest
        );

        // 4. 취소 성공 처리
        if ("CANCELED".equals(tossResponse.getStatus())) {
            payment.refund();

            // 주문 상태 업데이트
            Order order = payment.getOrder();
            order.updatePaymentStatus(PaymentStatus.REFUNDED);
            order.updateStatus(OrderStatus.CANCELLED);

            // 재고 복원
            order.getOrderItems().forEach(orderItem -> {
                if (orderItem.getProductVariant() != null) {
                    orderItem.getProductVariant().increaseStock(orderItem.getQuantity().longValue());
                }
            });

            log.info("토스페이먼츠 결제 취소 완료 - paymentKey: {}", tossResponse.getPaymentKey());
        }

        return convertToResponseDto(payment);
    }

    /**
     * 토스페이먼츠 결제 수단을 enum으로 변환
     */
    private PaymentMethod convertTossMethodToEnum(String method) {
        if (method == null) {
            return PaymentMethod.CREDIT_CARD;
        }

        return switch (method) {
            case "카드" -> PaymentMethod.CREDIT_CARD;
            case "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "계좌이체" -> PaymentMethod.BANK_TRANSFER;
            case "휴대폰" -> PaymentMethod.MOBILE_PAYMENT;
            default -> PaymentMethod.CREDIT_CARD;
        };
    }

    /**
     * 토스페이먼츠 orderId에서 실제 주문 ID 추출
     * 형식: ORDER_{실제ID}_{타임스탬프} 또는 숫자만
     *
     * @param tossOrderId 토스페이먼츠 orderId
     * @return 실제 주문 ID
     */
    private Long extractOrderId(String tossOrderId) {
        try {
            // ORDER_1_1234567890 형식인 경우
            if (tossOrderId.startsWith("ORDER_")) {
                String[] parts = tossOrderId.split("_");
                if (parts.length >= 2) {
                    return Long.parseLong(parts[1]);
                }
            }
            // 숫자만 있는 경우
            return Long.parseLong(tossOrderId);
        } catch (NumberFormatException e) {
            log.error("orderId 파싱 실패: {}", tossOrderId, e);
            throw new BusinessException(ResultCode.INVALID_REQUEST);
        }
    }

    /**
     * 결제 완료된 주문의 상품들을 장바구니에서 삭제
     * 주문 항목의 상품 variant를 기준으로 사용자 장바구니에서 찾아 삭제
     *
     * @param order 결제 완료된 주문
     */
    private void deleteCartItemsForCompletedOrder(Order order) {
        order.getOrderItems().forEach(orderItem -> {
            if (orderItem.getProductVariant() != null) {
                // 사용자의 장바구니에서 해당 상품 variant를 가진 아이템 찾기
                CartItem cartItem = cartItemRepository.findByUserAndProductVariant(
                        order.getUser(),
                        orderItem.getProductVariant()
                ).orElse(null);

                if (cartItem != null) {
                    cartItemRepository.delete(cartItem);
                    log.info("장바구니 삭제 - 사용자 ID: {}, 상품변형 ID: {}",
                            order.getUser().getId(), orderItem.getProductVariant().getId());
                }
            }
        });
    }
}