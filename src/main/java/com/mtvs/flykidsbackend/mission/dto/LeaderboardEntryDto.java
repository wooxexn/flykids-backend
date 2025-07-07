package com.mtvs.flykidsbackend.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 리더보드 상위 유저 DTO
 * - 특정 미션에 대해 점수 순으로 정렬된 유저 정보
 */
@Getter
@Builder
@AllArgsConstructor
public class LeaderboardEntryDto {

    @Schema(description = "순위 (1부터 시작)", example = "1")
    private int rank;

    @Schema(description = "유저 닉네임", example = "우선")
    private String nickname;

    @Schema(description = "점수", example = "95")
    private int score;

    @Schema(description = "소요 시간 (초)", example = "40.2")
    private double totalTime;

    @Schema(description = "미션 완료 시간", example = "2025-07-07T12:30:00")
    private LocalDateTime completedAt;
}
