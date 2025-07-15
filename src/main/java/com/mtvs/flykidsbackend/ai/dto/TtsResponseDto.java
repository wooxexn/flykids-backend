package com.mtvs.flykidsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * TTS 응답용 DTO
 * - TTS 서버에서 반환한 mp3 파일 URL을 클라이언트에게 전달
 */
@Data
public class TtsResponseDto {

    @Schema(
            description = "TTS로 생성된 음성 파일(mp3)의 S3 URL",
            example = "https://flykids-audio-files.s3.ap-northeast-2.amazonaws.com/mission1_intro.mp3"
    )
    private String audioUrl;
}
