package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 미션 수행 결과 저장 요청 DTO
 * - 클라이언트로부터 전달되는 결과 정보
 * - 점수는 서버에서 계산되므로 포함하지 않음
 */
@Getter
@Setter
public class DroneMissionResultRequestDto {

    @Schema(description = "사용한 드론 ID", example = "1", required = true)
    private Long droneId;

    @Schema(description = "총 비행 시간 (단위: 초)", example = "42.5")
    private double totalTime;

    @Schema(description = "미션 아이템별 수행 결과 리스트")
    private List<MissionItemResult> itemResults;

    @Getter
    @Setter
    public static class MissionItemResult {

        @Schema(description = "미션 아이템 유형", example = "COIN")
        private MissionType type;

        @Schema(description = "미션 아이템별 소요 시간 (단위: 초)", example = "15.0")
        private double itemTime;

        @Schema(description = "경로 이탈 횟수", example = "1")
        private int deviationCount;

        @Schema(description = "장애물 충돌 횟수", example = "0")
        private int collisionCount;
    }
}
