package com.mtvs.flykidsbackend.ai.dto;

import lombok.*;

/**
 * TTS 응답 DTO
 * - AI 서버로부터 반환된 음성 파일 URL을 담는 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TtsResponseDto {

    /** 생성된 음성 파일의 URL (ex: /audio/voice123.mp3) */
    private String audioUrl;
}
