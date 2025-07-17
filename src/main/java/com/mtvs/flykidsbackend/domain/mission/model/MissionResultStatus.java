package com.mtvs.flykidsbackend.domain.mission.model;

/**
 * 미션 결과 상태를 나타내는 열거형(Enum)
 * <p>각 미션 결과는 성공(SUCCESS), 실패(FAIL), 중단(ABORT) 중 하나의 상태를 가진다.</p>
 */
public enum MissionResultStatus {
    /**
     * 미션 성공
     */
    SUCCESS,

    /**
     * 미션 실패
     */
    FAIL,

    /**
     * 미션 중단 (사용자가 도중에 종료한 경우)
     */
    ABORT,

    /**
     * 미션 미시도 (아직 시도하지 않은 상태)
     */
    NOT_ATTEMPTED
}
