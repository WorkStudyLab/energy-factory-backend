package com.energyfactory.energy_factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSE 알림 DTO
 * 주문 상태 변경 등의 실시간 알림을 전송하는 데 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    /**
     * 알림 타입 (ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED)
     */
    private String type;

    /**
     * 알림 제목
     */
    private String title;

    /**
     * 알림 메시지
     */
    private String message;

    /**
     * 주문 ID (주문 관련 알림인 경우)
     */
    private Long orderId;

    /**
     * 주문 번호 (주문 관련 알림인 경우)
     */
    private Long orderNumber;

    /**
     * 알림 발송 시간
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 주문 상태 변경 알림 생성 헬퍼 메서드
     */
    public static NotificationDto ofOrderStatus(String type, String title, String message, Long orderId, Long orderNumber) {
        return NotificationDto.builder()
                .type(type)
                .title(title)
                .message(message)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
