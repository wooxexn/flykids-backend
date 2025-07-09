package com.mtvs.flykidsbackend.ai.service;

import com.mtvs.flykidsbackend.ai.dto.TtsRequestDto;
import com.mtvs.flykidsbackend.ai.dto.TtsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * TTS 요청 서비스
 * - 백엔드에서 AI 서버로 텍스트를 전달하고 음성 URL을 받아오는 역할
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    private final WebClient webClient;

    @Value("${ai.tts.url}")
    private String ttsApiUrl;

    /**
     * TTS 요청을 보내고 음성 파일 URL을 응답받음
     *
     * @param requestDto TTS 요청 정보 (userId, missionId, status, message)
     * @return TTS 응답 DTO (음성 파일 URL 포함)
     */
    public TtsResponseDto sendTtsRequest(TtsRequestDto requestDto) {
        return webClient.post()
                .uri(ttsApiUrl)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(TtsResponseDto.class)
                .onErrorResume(e -> {
                    // 로그 찍고 fallback
                    log.warn("TTS 요청 실패: {}", e.getMessage());
                    return Mono.just(TtsResponseDto.builder()
                            .audioUrl("")
                            .build());
                })
                .block();  // 동기 방식으로 대기
    }
}
