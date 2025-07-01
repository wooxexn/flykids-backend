package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 미션 수행 결과 저장 요청 DTO
 * - 클라이언트로부터 전달되는 결과 정보
 * - 미션 유형별로 필요한 추가 데이터 포함
 *   - COIN: collectedCoinCount (수집한 코인 개수)
 *   - OBSTACLE: deviationCount, collisionCount 등 기본 필드 사용
 *   - PHOTO: photoCaptured (사진 촬영 성공 여부)
 * - 점수는 서버에서 계산하므로 클라이언트에선 보내지 않음
 */
@Getter
@Setter
public class DroneMissionResultRequestDto {

    /** 사용한 드론 ID */
    @Schema(description = "사용한 드론 ID", example = "1", required = true)
    private Long droneId;

    /** 총 비행 시간 (초 단위) */
    @Schema(description = "총 비행 시간 (단위: 초)", example = "42.5")
    private double totalTime;

    /** 경로 이탈 횟수 (OBSTACLE 미션에 사용) */
    @Schema(description = "경로 이탈 횟수", example = "2")
    private int deviationCount;

    /** 장애물 충돌 횟수 (OBSTACLE 미션에 사용) */
    @Schema(description = "장애물 충돌 횟수", example = "1")
    private int collisionCount;

    /** 코인 미션에서 수집한 코인 개수 (COIN 미션에 사용) */
    @Schema(description = "코인 미션 수집 코인 개수", example = "10")
    private Integer collectedCoinCount;

    /** 사진 미션에서 사진 촬영 성공 여부 (PHOTO 미션에 사용) */
    @Schema(description = "사진 미션 촬영 성공 여부", example = "true")
    private Boolean photoCaptured;

    /** 미션 유형 (COIN, OBSTACLE, PHOTO) */
    @Schema(description = "미션 유형 (COIN, OBSTACLE, PHOTO)", example = "COIN")
    private MissionType missionType;
}
