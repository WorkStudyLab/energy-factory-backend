package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.PaymentCreateRequestDto;
import com.energyfactory.energy_factory.dto.PaymentResponseDto;
import com.energyfactory.energy_factory.entity.Order;
import com.energyfactory.energy_factory.entity.Payment;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.OrderRepository;
import com.energyfactory.energy_factory.repository.PaymentRepository;
import com.energyfactory.energy_factory.utils.enums.OrderStatus;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
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
        if (!order.getTotalPrice().equals(requestDto.getAmount())) {
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
                orderItem.getProduct().increaseStock(orderItem.getQuantity());
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
}