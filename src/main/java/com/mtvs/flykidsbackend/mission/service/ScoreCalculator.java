package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
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

    /**
     * 미션 성공 여부 판단
     * - 미션 타입별로 성공 조건을 달리 적용한다.
     *
     * @param type        미션 유형 (COIN, OBSTACLE, PHOTO 등)
     * @param dto         클라이언트에서 전달받은 미션 아이템별 결과 데이터
     * @param missionItem DB에 저장된 미션 아이템 정보 (성공 기준 데이터 포함)
     * @return 성공 여부 (true: 성공, false: 실패)
     * @throws IllegalArgumentException 지원하지 않는 미션 유형인 경우 발생
     */
    public boolean isMissionSuccess(MissionType type, DroneMissionResultRequestDto.MissionItemResult dto, MissionItem missionItem) {
        switch (type) {
            // COIN 미션은 수집한 코인 개수가 총 코인 개수와 일치해야 성공
            case COIN:
                return dto.getCollectedCoinCount() != null
                        && dto.getCollectedCoinCount() == (missionItem.getTotalCoinCount() != null ? missionItem.getTotalCoinCount() : 0);
            case OBSTACLE:
                // OBSTACLE 미션은 충돌 횟수가 3회 미만일 경우 성공 처리
                return dto.getCollisionCount() < 3;
            case PHOTO:
                // PHOTO 미션은 사진 촬영 여부가 true일 경우 성공 처리
                return dto.getPhotoCaptured() != null && dto.getPhotoCaptured();
            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
    }
}
