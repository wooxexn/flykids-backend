package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 미션 수행 기록 응답 DTO
 * - 로그인한 유저가 완료한 미션 결과 목록을 응답할 때 사용
 * - DroneMissionResult + Mission 엔티티 기반으로 구성됨
 */
@Getter
@Builder
@AllArgsConstructor
public class MissionHistoryResponseDto {

    @Schema(description = "미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "미션 이름", example = "코인 먹기")
    private String missionName;

    @Schema(description = "미션 유형", example = "COIN")
    private MissionType missionType;

    @Schema(description = "최종 점수 (0~100)", example = "87")
    private int score;

    @Schema(description = "총 소요 시간 (초 단위)", example = "42.5")
    private double totalTime;

    @Schema(description = "경로 이탈 횟수", example = "2")
    private int deviationCount;

    @Schema(description = "장애물 충돌 횟수", example = "1")
    private int collisionCount;

    @Schema(description = "미션 완료 시각", example = "2025-07-07T10:15:30")
    private LocalDateTime completedAt;
}
