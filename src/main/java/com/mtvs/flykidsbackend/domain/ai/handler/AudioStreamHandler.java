package com.mtvs.flykidsbackend.domain.ai.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

/**
 * WebSocket 핸들러
 *
 * 유니티에서 전송한 음성 청크 데이터를 수신하고,
 * 이를 AI 서버로 중계한 후, AI 서버가 반환한 음성(mp3)을 유니티에 다시 전달하는 역할을 수행한다.
 */
@Slf4j
@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 유니티로부터 바이너리 메시지(음성 청크)가 도착했을 때 호출되는 메서드
     * @param session WebSocket 세션 객체
     * @param message 클라이언트가 전송한 바이너리 메시지 (mp3/wav 청크)
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] audioChunk = message.getPayload().array();
        log.info("🎧 유니티로부터 수신한 청크 크기: {} bytes", audioChunk.length);

        // 1. AI 서버에 청크 바이너리 전송
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

        // 2. 유니티로 mp3 파일 바이너리 응답
        session.sendMessage(new BinaryMessage(mp3Response));
        log.info("유니티에 mp3 응답 전송 완료");
    }

    /**
     * WebSocket 연결이 최초로 열렸을 때 호출되는 메서드
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket 연결됨: 세션 ID = {}", session.getId());
    }

    /**
     * WebSocket 연결이 종료되었을 때 호출되는 메서드
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 연결 종료됨: 세션 ID = {}", session.getId());
    }
}
