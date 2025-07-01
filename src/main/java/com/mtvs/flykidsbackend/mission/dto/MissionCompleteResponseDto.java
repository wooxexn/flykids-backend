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

    @Schema(description = "TTS 피드백용 문장", example = "미션 완료! 90점입니다. 2회 이탈했습니다.")
    private String message;
}