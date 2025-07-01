package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    /**
     * 미션 유형에 따라 점수를 계산한다.
     *
     * @param type 미션 유형 (COIN / OBSTACLE / PHOTO)
     * @param totalTime 총 소요 시간 (초)
     * @param deviationCount 경로 이탈 횟수
     * @param coinCount 코인 미션일 경우 획득한 코인 개수
     * @return 계산된 점수 (정수)
     */
    public int calculateScore(MissionType type, double totalTime, int deviationCount, int coinCount) {
        return switch (type) {
            case COIN -> calculateCoinScore(totalTime, coinCount);
            case OBSTACLE -> calculateObstacleScore(deviationCount);
            case PHOTO -> calculatePhotoScore(deviationCount);
        };
    }

    /**
     * 코인 먹기 미션 점수 계산
     * - 기본 계산식: 코인 1개당 10점
     * - 시간 초과 시 페널티 적용 (0.5점/초)
     */
    private int calculateCoinScore(double totalTime, int coinCount) {
        int baseScore = coinCount * 10;
        int penalty = (int) (totalTime * 0.5);
        return Math.max(0, baseScore - penalty);
    }

    /**
     * 장애물 피하기 미션 점수 계산
     * - 기본 점수: 100점
     * - 이탈 1회당 5점 감점
     */
    private int calculateObstacleScore(int deviationCount) {
        return Math.max(0, 100 - (deviationCount * 5));
    }

    /**
     * 사진 찍기 미션 점수 계산
     * - 이탈 없음: 100점
     * - 이탈 있음: 80점
     */
    private int calculatePhotoScore(int deviationCount) {
        return deviationCount == 0 ? 100 : 80;
    }
}
