package com.mtvs.flykidsbackend.domain.ai.controller;

import com.mtvs.flykidsbackend.domain.ai.dto.TtsRequestDto;
import com.mtvs.flykidsbackend.domain.ai.dto.TtsResponseDto;
import com.mtvs.flykidsbackend.domain.ai.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AI 서버와 연동하여 음성 처리를 담당하는 컨트롤러
 */
@Tag(
        name = "Voice Feedback API",
        description = "음성 입력을 받아 AI 서버에서 처리 후 음성 응답을 반환하는 API"
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class VoiceFeedbackController {

    private final TtsService ttsService;
    private final RestTemplate restTemplate;

    private static final String AI_SERVER_URL = "http://221.163.19.142:58014/api/v1/chatbot/audio";

    @Operation(
            summary = "텍스트 → 음성(mp3) 변환 요청",
            description = "사용자의 텍스트 요청을 AI 서버로 전송하여 TTS(mp3) 음성 URL을 반환합니다."
    )
    @PostMapping("/voice-feedback")
    @CrossOrigin(origins = "*")
    public ResponseEntity<TtsResponseDto> getVoiceFeedback(@RequestBody TtsRequestDto requestDto) {
        TtsResponseDto responseDto = ttsService.requestTts(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Unity에서 발생하는 OPTIONS preflight 요청 처리
     */
    @RequestMapping(value = "/audio-stream", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptionsRequest() {
        log.info("OPTIONS preflight 요청 수신 - Unity CORS 처리");

        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    @Operation(
            summary = "음성 파일 업로드 및 AI 응답 스트리밍 반환",
            description = "WAV 음성 파일을 업로드하면 AI 서버에서 STT → 챗봇 처리 → TTS 과정을 거쳐 음성 응답(WAV)을 스트리밍 방식으로 청크 단위 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 처리 - AI 서버로부터 음성 응답 스트리밍",
                    content = @Content(
                            mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Content-Type 오류 또는 빈 데이터"),
            @ApiResponse(responseCode = "403", description = "CORS 오류 - 도메인 접근 권한 없음"),
            @ApiResponse(responseCode = "422", description = "요청 파라미터 오류 (WAV 형식 오류 등)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping(
            value = "/audio-stream",
            consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "audio/wav", "audio/*"},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<StreamingResponseBody> processVoiceInput(
            @RequestBody byte[] audioData,
            HttpServletRequest request) {

        String contentType = request.getContentType();
        log.info("음성 데이터 수신 - 크기: {} bytes", audioData.length);
        log.info("Content-Type: {}", contentType);

        // Content-Type 검증
        if (contentType == null || (!contentType.equals("application/octet-stream") && !contentType.startsWith("audio/"))) {
            log.warn("잘못된 Content-Type: {}, application/octet-stream 또는 audio/* 이어야 함", contentType);
            return ResponseEntity.badRequest()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(outputStream -> {
                        outputStream.write("Content-Type must be application/octet-stream or audio/*".getBytes());
                    });
        }

        // 입력 데이터 검증
        if (audioData == null || audioData.length == 0) {
            log.warn("빈 음성 데이터 수신");
            return ResponseEntity.badRequest()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "POST, OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");

        StreamingResponseBody stream = outputStream -> {
            try {
                // AI 서버 요청 헤더 설정 (403 오류 방지)
                HttpHeaders aiHeaders = new HttpHeaders();
                aiHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                aiHeaders.setContentLength(audioData.length);
                aiHeaders.add("User-Agent", "FlyKids-Backend/1.0");
                aiHeaders.add("Accept", "*/*");
                aiHeaders.add("Connection", "keep-alive");

                HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioData, aiHeaders);

                log.info("AI 서버로 음성 데이터 전송 시작 - URL: {}", AI_SERVER_URL);

                ResponseEntity<byte[]> aiResponse = restTemplate.exchange(
                        AI_SERVER_URL,
                        HttpMethod.POST,
                        requestEntity,
                        byte[].class
                );

                byte[] responseBytes = aiResponse.getBody();

                if (responseBytes == null || responseBytes.length == 0) {
                    log.warn("AI 서버로부터 빈 응답 수신");
                    return;
                }

                // 청크 단위로 응답 스트리밍 처리
                int chunkSize = 4096;
                int offset = 0;
                while (offset < responseBytes.length) {
                    int size = Math.min(chunkSize, responseBytes.length - offset);
                    outputStream.write(responseBytes, offset, size);
                    outputStream.flush();
                    offset += size;
                    try {
                        Thread.sleep(10); // 네트워크 상황에 따라 조절 가능
                    } catch (InterruptedException ignored) {}
                }

                log.info("AI 서버로부터 음성 응답 스트리밍 완료");

            } catch (Exception e) {
                log.error("AI 서버 통신 중 오류 발생 - URL: {}, Error: {}", AI_SERVER_URL, e.getMessage(), e);
                throw e;
            }
        };

        return new ResponseEntity<>(stream, responseHeaders, HttpStatus.OK);
    }
}
