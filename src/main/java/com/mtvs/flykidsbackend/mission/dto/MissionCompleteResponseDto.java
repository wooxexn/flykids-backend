package com.mtvs.flykidsbackend.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 미션 완료 응답 DTO
 * - 점수, 소요 시간, 경로 이탈 횟수, TTS 피드백 문장을 포함한다.
 */
@Getter
@Builder
@AllArgsConstructor
public class MissionCompleteResponseDto {

    @Schema(description = "최종 점수 (0~100)")
    private int score;

    @Schema(description = "총 비행 시간 (초 단위)", example = "113.5")
    private double duration;

    @Schema(description = "경로 이탈 횟수", example = "2")
    private int deviationCount;

    @Schema(description = "충돌 횟수", example = "1")
    private int collisionCount;

    @Schema(description = "미션 성공 여부", example = "true")
    private boolean success;

    @Schema(
            description = "TTS용 문장 (줄바꿈 및 특수기호 제거됨, 음성 생성에 적합한 형식)",
            example = "모든 미션 아이템 성공 COIN 미션 성공 OBSTACLE 미션 성공 PHOTO 미션 성공"
    )
    private String message;

    @Schema(
            description = "원본 메시지 (줄바꿈 및 특수기호 포함, 사람이 읽기 좋은 형식)",
            example = "모든 미션 아이템 성공!\n[COIN 미션] 성공\n[OBSTACLE 미션] 성공\n[PHOTO 미션] 성공"
    )
    private String rawMessage;

    @Schema(description = "TTS 음성 URL", example = "/audio/voice123.mp3")
    private String audioUrl;
}