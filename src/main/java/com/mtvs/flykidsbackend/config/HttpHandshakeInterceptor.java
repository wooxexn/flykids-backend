package com.mtvs.flykidsbackend.config;

import com.mtvs.flykidsbackend.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            String query = uri.getQuery(); // 예: token=eyJhbGciOiJIUzI1NiJ9...

            if (query != null && query.startsWith("token=")) {
                String token = query.substring(6); // token= 제거
                log.info("[WS 인터셉터] 받은 토큰: {}", token);

                if (jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getUserIdFromToken(token);
                    attributes.put("userId", userId);
                    log.info("[WS 인터셉터] 인증 성공: userId={}", userId);
                    return true;
                } else {
                    log.warn("[WS 인터셉터] JWT 토큰 유효성 실패");
                }
            } else {
                log.warn("[WS 인터셉터] Query에 token 없음: {}", query);
            }

            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;

        } catch (Exception e) {
            log.error("[WS 인터셉터] 예외 발생", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 생략 가능 (afterHandshake 필요시 작성)
    }
}
