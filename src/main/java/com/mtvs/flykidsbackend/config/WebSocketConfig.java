package com.mtvs.flykidsbackend.config;

import com.mtvs.flykidsbackend.domain.ai.handler.AudioStreamHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket 서버 설정 클래스
 *
 * 유니티 클라이언트로부터 음성 청크를 실시간으로 전송받기 위한 WebSocket 엔드포인트를 등록한다.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AudioStreamHandler audioStreamHandler;
    private final HttpHandshakeInterceptor handshakeInterceptor;

    /**
     * WebSocket 핸들러를 등록하는 메서드
     * "/ws/audio" 경로로 들어오는 WebSocket 요청을 audioStreamHandler가 처리하도록 설정한다.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(audioStreamHandler, "/ws/audio")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*"); // CORS 정책 허용 (유니티 클라이언트에서 접속 가능)
    }
}
