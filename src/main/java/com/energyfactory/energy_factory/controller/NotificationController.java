package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 알림 컨트롤러
 * SSE(Server-Sent Events)를 통한 실시간 알림 기능 제공
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "실시간 알림 API (SSE)")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "SSE 알림 스트림 연결",
            description = "실시간 알림을 받기 위한 SSE 연결을 생성합니다.\n\n" +
                    "- 주문 상태 변경 시 자동으로 알림 수신\n" +
                    "- 연결은 30분간 유지되며, 이후 자동 재연결 필요\n" +
                    "- EventSource API를 사용하여 연결\n\n" +
                    "**프론트엔드 예시:**\n" +
                    "```javascript\n" +
                    "const eventSource = new EventSource('/api/notifications/stream');\n" +
                    "eventSource.addEventListener('notification', (event) => {\n" +
                    "  const data = JSON.parse(event.data);\n" +
                    "  console.log(data.title, data.message);\n" +
                    "});\n" +
                    "```"
    )
    public SseEmitter streamNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return notificationService.createEmitter(userId);
    }

    @GetMapping("/stats")
    @Operation(
            summary = "알림 통계 조회",
            description = "현재 연결된 사용자 수를 조회합니다."
    )
    public int getConnectedUserCount() {
        return notificationService.getConnectedUserCount();
    }
}
