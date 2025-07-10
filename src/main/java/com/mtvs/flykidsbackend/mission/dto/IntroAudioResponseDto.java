package com.mtvs.flykidsbackend.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 미션 시작 멘트의 음성 URL 응답 DTO
 */
@Getter
@Builder
public class IntroAudioResponseDto {

    @Schema(description = "미션 ID", example = "1")
    private Long missionId;

    @Schema(
            description = "TTS로 생성된 음성 파일 URL",
            example = "https://flykids-tts-files.s3.ap-northeast-2.amazonaws.com/mission1_intro.mp3"
    )
    private String audioUrl;
}
