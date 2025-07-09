package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import org.springframework.stereotype.Component;

/**
 * 미션 점수 계산 및 성공 여부 판별 컴포넌트
 * - 미션 유형별로 점수 계산 방식과 성공 조건을 분기 처리한다.
 */
@Component
public class ScoreCalculator {

    /**
     * 미션 점수 계산
     * - COIN: 코인 개수 × 10점 - 시간 초과 페널티
     * - OBSTACLE: 100점 - (이탈 횟수 × 5점)
     * - PHOTO: 0점 (점수 없음)
     *
     * @param type 미션 타입
     * @param dto 클라이언트에서 전달받은 미션 수행 결과
     * @return 계산된 점수 (0 ~ 100)
     */
    public int calculateScore(MissionType type, DroneMissionResultRequestDto dto) {
        DroneMissionResultRequestDto.MissionItemResult item = dto.getItemResult();

        return switch (type) {
            case COIN -> calculateCoinScore(item.getTotalTime(), item.getCollectedCoinCount());
            case OBSTACLE -> calculateObstacleScore(item.getDeviationCount());
            case PHOTO -> 0;
            default -> throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
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
     * 미션 성공 여부 판별
     * - COIN: 수집한 코인 개수가 Mission 기준과 동일해야 성공
     * - OBSTACLE: 충돌 횟수가 3회 미만이면 성공
     * - PHOTO: 사진 촬영 성공 여부가 true여야 성공
     *
     * @param type 미션 타입
     * @param dto 클라이언트에서 전달받은 미션 결과
     * @param mission DB에 저장된 기준 미션 정보
     * @return 성공 여부
     */
    public boolean isMissionSuccess(MissionType type, DroneMissionResultRequestDto dto, Mission mission) {
        DroneMissionResultRequestDto.MissionItemResult item = dto.getItemResult();

        return switch (type) {
            case COIN -> item.getCollectedCoinCount() != null &&
                    item.getCollectedCoinCount().equals(mission.getTotalCoinCount());
            case OBSTACLE -> item.getCollisionCount() < 3;
            case PHOTO -> Boolean.TRUE.equals(item.getPhotoCaptured());
            default -> throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        };
    }

}
