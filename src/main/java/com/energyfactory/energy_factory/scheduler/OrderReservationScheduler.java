package com.energyfactory.energy_factory.scheduler;

import com.energyfactory.energy_factory.entity.Order;
import com.energyfactory.energy_factory.entity.OrderItem;
import com.energyfactory.energy_factory.entity.ProductVariant;
import com.energyfactory.energy_factory.repository.OrderRepository;
import com.energyfactory.energy_factory.utils.enums.OrderStatus;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 재고 예약 타임아웃 스케줄러
 *
 * 일정 시간이 지난 PENDING 상태의 주문에 대해 자동으로 예약을 해제합니다.
 * - 타임아웃 시간: 15분
 * - 실행 주기: 5분마다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReservationScheduler {

    private final OrderRepository orderRepository;

    // 재고 예약 타임아웃 시간 (분)
    private static final int RESERVATION_TIMEOUT_MINUTES = 15;

    /**
     * 타임아웃된 주문의 재고 예약 자동 해제
     *
     * 매 5분마다 실행되며, 15분이 지난 PENDING 주문을 찾아 예약을 해제합니다.
     */
    @Scheduled(cron = "0 */5 * * * *") // 5분마다 실행
    @Transactional
    public void releaseTimeoutReservations() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(RESERVATION_TIMEOUT_MINUTES);

            // 타임아웃된 PENDING 주문 조회
            List<Order> timeoutOrders = orderRepository.findTimeoutOrders(
                PaymentStatus.PENDING,
                cutoffTime
            );

            if (timeoutOrders.isEmpty()) {
                log.debug("타임아웃된 주문 없음");
                return;
            }

            log.info("타임아웃된 주문 {}건 발견, 재고 예약 해제 시작", timeoutOrders.size());

            int processedCount = 0;
            int errorCount = 0;

            for (Order order : timeoutOrders) {
                try {
                    // 재고 예약 해제
                    for (OrderItem orderItem : order.getOrderItems()) {
                        ProductVariant variant = orderItem.getProductVariant();
                        if (variant != null) {
                            variant.releaseReservedStock(orderItem.getQuantity().longValue());
                            log.debug("재고 예약 해제 - 주문번호: {}, 상품변형 ID: {}, 수량: {}",
                                order.getOrderNumber(),
                                variant.getId(),
                                orderItem.getQuantity());
                        }
                    }

                    // 주문 상태 변경
                    order.updatePaymentStatus(PaymentStatus.FAILED);
                    order.updateStatus(OrderStatus.CANCELLED);

                    processedCount++;

                } catch (Exception e) {
                    errorCount++;
                    log.error("주문 {}번 재고 예약 해제 실패: {}", order.getOrderNumber(), e.getMessage(), e);
                }
            }

            log.info("재고 예약 해제 완료 - 성공: {}건, 실패: {}건", processedCount, errorCount);

        } catch (Exception e) {
            log.error("재고 예약 해제 스케줄러 실행 중 오류 발생", e);
        }
    }
}
