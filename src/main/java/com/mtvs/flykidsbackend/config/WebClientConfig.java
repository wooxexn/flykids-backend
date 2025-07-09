package com.mtvs.flykidsbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 전역 설정 클래스
 * - 외부 API (TTS 서버 등) 호출에 사용됨
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
