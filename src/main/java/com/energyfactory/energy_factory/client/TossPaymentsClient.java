package com.energyfactory.energy_factory.client;

import com.energyfactory.energy_factory.config.TossPaymentsConfig;
import com.energyfactory.energy_factory.dto.TossPaymentCancelRequestDto;
import com.energyfactory.energy_factory.dto.TossPaymentConfirmRequestDto;
import com.energyfactory.energy_factory.dto.TossPaymentResponseDto;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 토스페이먼츠 API 호출 클라이언트
 * WebClient를 사용하여 토스페이먼츠 REST API와 통신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final WebClient tossPaymentsWebClient;
    private final TossPaymentsConfig tossPaymentsConfig;

    /**
     * Basic Auth 헤더 생성
     * 토스페이먼츠는 시크릿 키를 Base64 인코딩하여 Authorization 헤더로 전송
     */
    private String getAuthorizationHeader() {
        String credentials = tossPaymentsConfig.getSecretKey() + ":";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }

    /**
     * 결제 승인 API 호출
     *
     * @param request 결제 승인 요청 정보 (서버에서 계산한 금액 포함)
     * @return 토스페이먼츠 결제 응답
     */
    public TossPaymentResponseDto confirmPayment(TossPaymentConfirmRequestDto request) {
        log.info("토스페이먼츠 결제 승인 API 호출 - paymentKey: {}, orderId: {}, amount: {}",
                request.getPaymentKey(), request.getOrderId(), request.getAmount());

        try {
            TossPaymentResponseDto response = tossPaymentsWebClient
                    .post()
                    .uri("/v1/payments/confirm")
                    .header("Authorization", getAuthorizationHeader())
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(TossPaymentResponseDto.class)
                                .flatMap(errorBody -> {
                                    log.error("토스페이먼츠 4xx 에러 - code: {}, message: {}",
                                            errorBody.getCode(), errorBody.getMessage());
                                    return Mono.error(new BusinessException(ResultCode.PAYMENT_FAILED));
                                })
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("토스페이먼츠 5xx 서버 에러");
                        return Mono.error(new BusinessException(ResultCode.PAYMENT_FAILED));
                    })
                    .bodyToMono(TossPaymentResponseDto.class)
                    .block();

            log.info("토스페이먼츠 결제 승인 성공 - paymentKey: {}, status: {}",
                    response.getPaymentKey(), response.getStatus());

            return response;

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패", e);
            throw new BusinessException(ResultCode.PAYMENT_FAILED);
        }
    }

    /**
     * 결제 조회 API 호출
     *
     * @param paymentKey 토스페이먼츠 결제 키
     * @return 토스페이먼츠 결제 응답
     */
    public TossPaymentResponseDto getPayment(String paymentKey) {
        log.info("토스페이먼츠 결제 조회 - paymentKey: {}", paymentKey);

        try {
            TossPaymentResponseDto response = tossPaymentsWebClient
                    .get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .header("Authorization", getAuthorizationHeader())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("토스페이먼츠 결제 조회 실패");
                        return Mono.error(new BusinessException(ResultCode.NOT_FOUND));
                    })
                    .bodyToMono(TossPaymentResponseDto.class)
                    .block();

            log.info("토스페이먼츠 결제 조회 성공 - status: {}", response.getStatus());
            return response;

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 조회 실패", e);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    /**
     * 결제 취소 API 호출
     *
     * @param paymentKey 토스페이먼츠 결제 키
     * @param request 취소 요청 정보
     * @return 토스페이먼츠 결제 응답
     */
    public TossPaymentResponseDto cancelPayment(String paymentKey, TossPaymentCancelRequestDto request) {
        log.info("토스페이먼츠 결제 취소 요청 - paymentKey: {}, reason: {}",
                paymentKey, request.getCancelReason());

        try {
            TossPaymentResponseDto response = tossPaymentsWebClient
                    .post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .header("Authorization", getAuthorizationHeader())
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(TossPaymentResponseDto.class)
                                .flatMap(errorBody -> {
                                    log.error("토스페이먼츠 취소 실패 - code: {}, message: {}",
                                            errorBody.getCode(), errorBody.getMessage());
                                    return Mono.error(new BusinessException(ResultCode.PAYMENT_CANCEL_FAILED));
                                })
                    )
                    .bodyToMono(TossPaymentResponseDto.class)
                    .block();

            log.info("토스페이먼츠 결제 취소 성공 - status: {}", response.getStatus());
            return response;

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패", e);
            throw new BusinessException(ResultCode.PAYMENT_CANCEL_FAILED);
        }
    }
}
