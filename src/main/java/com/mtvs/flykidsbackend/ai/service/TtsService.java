package com.mtvs.flykidsbackend.ai.service;

import com.mtvs.flykidsbackend.ai.dto.TtsRequestDto;
import com.mtvs.flykidsbackend.ai.dto.TtsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * TTS 서버와 통신하여 mp3 음성 URL을 받아오는 서비스
 */
@Service
@RequiredArgsConstructor
public class TtsService {

    private final RestTemplate restTemplate;

    @Value("${ai.tts.url}")
    private String TTS_SERVER_URL;

    public TtsResponseDto requestTts(TtsRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TtsRequestDto> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<TtsResponseDto> response = restTemplate.exchange(
                TTS_SERVER_URL,
                HttpMethod.POST,
                request,
                TtsResponseDto.class
        );

        return response.getBody();
    }
}
