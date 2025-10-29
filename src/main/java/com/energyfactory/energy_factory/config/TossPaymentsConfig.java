package com.energyfactory.energy_factory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 토스페이먼츠 설정 클래스
 * application.yml의 toss.payments 속성을 바인딩
 */
@Configuration
@ConfigurationProperties(prefix = "toss.payments")
@Getter
@Setter
public class TossPaymentsConfig {

    /**
     * 클라이언트 키 (프론트엔드에서 사용)
     */
    private String clientKey;

    /**
     * 시크릿 키 (백엔드 API 호출 시 사용)
     */
    private String secretKey;

    /**
     * 토스페이먼츠 API 기본 URL
     */
    private String baseUrl;

    /**
     * 결제 성공 시 리다이렉트 URL
     */
    private String successUrl;

    /**
     * 결제 실패 시 리다이렉트 URL
     */
    private String failUrl;

    /**
     * 토스페이먼츠 API 호출용 WebClient 빈 생성
     */
    @Bean
    public WebClient tossPaymentsWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
