package com.mtvs.flykidsbackend.mission.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 미션 유형 ENUM
 * - 미션의 종류에 따라 점수 계산 방식이 달라지므로 구분 필요
 * - COIN: 코인 수집 미션
 * - OBSTACLE: 장애물 피하기 미션
 * - PHOTO: 특정 위치에서 사진 촬영 미션
 * - COMPLEX: 여러 미션을 조합한 복합 미션
 */
public enum MissionType {
    COIN,
    OBSTACLE,
    PHOTO,
    COMPLEX;

    @JsonCreator
    public static MissionType from(String value) {
        if (value == null) return null;
        return MissionType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}