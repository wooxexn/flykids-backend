package com.mtvs.flykidsbackend.domain.ai.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Slf4j
@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] audioChunk = message.getPayload().array();
        log.info("유니티로부터 수신한 청크 크기: {} bytes", audioChunk.length);

        // AI 서버에 바이너리 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<byte[]> request = new HttpEntity<>(audioChunk, headers);

        ResponseEntity<byte[]> aiResponse;
        try {
            aiResponse = restTemplate.exchange(
                    "http://221.163.19.142:58011/api/v1/chatbot/audio",
                    HttpMethod.POST,
                    request,
                    byte[].class
            );
        } catch (Exception e) {
            log.error("AI 서버와 통신 중 오류 발생", e);
            session.sendMessage(new TextMessage("AI 서버 오류: " + e.getMessage()));
            return;
        }

        byte[] mp3Response = aiResponse.getBody();
        log.info("AI 서버로부터 mp3 응답 수신 ({} bytes)", mp3Response.length);

        // 유니티에 mp3 응답 전송
        session.sendMessage(new BinaryMessage(mp3Response));
        log.info("유니티에 mp3 응답 전송 완료");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket 연결 성공 - 세션 ID = {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 연결 종료됨: 세션 ID = {}", session.getId());
    }
}