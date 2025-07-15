package com.mtvs.flykidsbackend.ai.controller;

import com.mtvs.flykidsbackend.ai.dto.TtsRequestDto;
import com.mtvs.flykidsbackend.ai.dto.TtsResponseDto;
import com.mtvs.flykidsbackend.ai.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 유니티 → 텍스트 전달 → mp3 URL 반환용 컨트롤러
 */
@Tag(
        name = "Voice Feedback",
        description = "유저의 음성 데이터를 AI 서버에 전달하고, TTS로 생성된 음성(mp3) URL을 반환하는 API입니다."
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VoiceFeedbackController {

    private final TtsService ttsService;
    private final RestTemplate restTemplate;

    @Operation(
            summary = "텍스트 → 음성(mp3) 변환 요청",
            description = "사용자의 텍스트 요청을 AI 서버로 전송하여 TTS(mp3) 음성 URL을 반환합니다."
    )
    @PostMapping("/voice-feedback")
    public ResponseEntity<TtsResponseDto> getVoiceFeedback(@RequestBody TtsRequestDto requestDto) {
        TtsResponseDto responseDto = ttsService.requestTts(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "음성 파일(STT) 업로드",
            description = "유저의 음성(wav/mp3) 파일을 업로드하면, AI 서버에 전달하여 TTS 응답(mp3) URL을 반환합니다."
    )
    @PostMapping("/audio-stream")
    public ResponseEntity<TtsResponseDto> uploadVoice(@RequestParam("audioFile") MultipartFile audioFile) {
        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            String contentType = audioFile.getContentType();
            headers.setContentType(MediaType.valueOf(contentType));

            // 파일 바이트 추출
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioFile.getBytes(), headers);

            // AI 서버로 요청
            ResponseEntity<TtsResponseDto> response = restTemplate.exchange(
                    "http://221.163.19.142:58014/api/v1/chatbot/audio",
                    HttpMethod.POST,
                    requestEntity,
                    TtsResponseDto.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
