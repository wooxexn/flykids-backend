package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    /**
     * 미션 유형별 점수 계산
     * - COIN: 빠른 완료 + 이탈/충돌 최소화
     * - OBSTACLE: 이탈 및 충돌 감점이 큼
     * - PHOTO: 점수 개념 없음, 0점 처리
     *
     * @param type            미션 타입 (COIN / OBSTACLE / PHOTO)
     * @param totalTime       총 소요 시간 (초 단위)
     * @param deviationCount  경로 이탈 횟수
     * @param collisionCount  충돌 횟수
     * @return 계산된 점수 (0 ~ 100)
     */
    public int calculateScore(MissionType type, double totalTime, int deviationCount, int collisionCount, int collectedCoinCount) {
        switch (type) {
            case COIN:
                return calculateCoinScore(totalTime, collectedCoinCount);
            case OBSTACLE:
                return calculateObstacleScore(deviationCount);
            case PHOTO:
                return 0;
            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
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

}
