package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 알림 서비스
 * 사용자별 SSE Emitter를 관리하고 실시간 알림을 전송
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ObjectMapper objectMapper;

    /**
     * 사용자 ID별 SSE Emitter 저장소
     * ConcurrentHashMap을 사용하여 동시성 문제 해결
     */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결 타임아웃 (30분)
     */
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    /**
     * SSE Emitter 생성 및 저장
     *
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 기존 연결이 있으면 종료
        removeEmitter(userId);

        // 새 Emitter 저장
        emitters.put(userId, emitter);
        log.info("SSE 연결 생성: userId={}", userId);

        // 연결 완료 시 제거
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: userId={}", userId);
            emitters.remove(userId);
        });

        // 타임아웃 시 제거
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: userId={}", userId);
            emitters.remove(userId);
        });

        // 에러 발생 시 제거
        emitter.onError((e) -> {
            log.error("SSE 연결 에러: userId={}, error={}", userId, e.getMessage());
            emitters.remove(userId);
        });

        // 초기 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 성공"));
        } catch (IOException e) {
            log.error("초기 연결 메시지 전송 실패: userId={}", userId, e);
            emitters.remove(userId);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 전송
     *
     * @param userId 사용자 ID
     * @param notification 알림 내용
     */
    public void sendNotification(Long userId, NotificationDto notification) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.warn("SSE 연결이 없음: userId={}", userId);
            return;
        }

        try {
            // NotificationDto를 JSON으로 변환하여 전송
            String jsonData = objectMapper.writeValueAsString(notification);

            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(jsonData));

            log.info("알림 전송 성공: userId={}, type={}", userId, notification.getType());
        } catch (IOException e) {
            log.error("알림 전송 실패: userId={}, error={}", userId, e.getMessage());
            emitters.remove(userId);
        }
    }

    /**
     * 주문 상태 변경 알림 전송
     *
     * @param userId 사용자 ID
     * @param orderId 주문 ID
     * @param orderNumber 주문 번호
     * @param status 주문 상태
     */
    public void sendOrderNotification(Long userId, Long orderId, Long orderNumber, String status) {
        String type;
        String title;
        String message;

        switch (status) {
            case "CONFIRMED":
                type = "ORDER_CONFIRMED";
                title = "주문 확인";
                message = String.format("주문번호 %d번이 확인되었습니다.", orderNumber);
                break;
            case "SHIPPED":
                type = "ORDER_SHIPPED";
                title = "배송 시작";
                message = String.format("주문번호 %d번이 배송 시작되었습니다.", orderNumber);
                break;
            case "DELIVERED":
                type = "ORDER_DELIVERED";
                title = "배송 완료";
                message = String.format("주문번호 %d번이 배송 완료되었습니다.", orderNumber);
                break;
            case "CANCELLED":
                type = "ORDER_CANCELLED";
                title = "주문 취소";
                message = String.format("주문번호 %d번이 취소되었습니다.", orderNumber);
                break;
            default:
                log.warn("알 수 없는 주문 상태: status={}", status);
                return;
        }

        NotificationDto notification = NotificationDto.ofOrderStatus(
                type, title, message, orderId, orderNumber
        );

        sendNotification(userId, notification);
    }

    /**
     * 특정 사용자의 Emitter 제거
     *
     * @param userId 사용자 ID
     */
    public void removeEmitter(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("Emitter 종료 중 에러: userId={}", userId, e);
            }
        }
    }

    /**
     * 현재 연결된 사용자 수 조회
     *
     * @return 연결된 사용자 수
     */
    public int getConnectedUserCount() {
        return emitters.size();
    }
}
