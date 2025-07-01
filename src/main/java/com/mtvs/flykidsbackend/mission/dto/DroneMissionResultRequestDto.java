package com.mtvs.flykidsbackend.mission.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 미션 수행 결과 저장 요청 DTO
 * - 클라이언트로부터 전달되는 결과 정보
 * - 점수는 서버에서 계산되므로 포함하지 않음
 */
@Getter
@Setter
public class DroneMissionResultRequestDto {

    /** 사용한 드론 ID (필수) */
    private Long droneId;

    /** 총 비행 시간 (단위: 초) */
    private double totalTime;

    /** 경로 이탈 횟수 */
    private int deviationCount;
}
