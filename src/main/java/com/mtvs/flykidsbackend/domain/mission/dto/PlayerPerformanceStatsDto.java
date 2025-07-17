package com.mtvs.flykidsbackend.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 유저 미션 통계 응답 DTO
 * - 평균 점수, 총 수행 미션 수, 총 비행 시간 등 통계 데이터 제공
 */
@Getter
@Builder
@AllArgsConstructor
public class PlayerPerformanceStatsDto {

    @Schema(description = "총 미션 시도 횟수 (하위 미션 포함)", example = "12")
    private int totalAttempts;

    @Schema(description = "최종 승리 횟수 (3종 미션을 모두 성공한 세트 수)", example = "3")
    private int successfulSets;

    @Schema(description = "평균 점수 (0~100 사이)", example = "84.5")
    private double averageScore;

    @Schema(description = "총 비행 시간 (초)", example = "523.7")
    private double totalFlightTime;
}
