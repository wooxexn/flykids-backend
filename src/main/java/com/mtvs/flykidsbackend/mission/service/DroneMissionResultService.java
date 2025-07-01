package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * 드론 미션 결과 처리 서비스
 * - 점수 계산 및 결과 저장 로직 담당
 */
@Service
@RequiredArgsConstructor
public class DroneMissionResultService {

    private final DroneMissionResultRepository resultRepository;
    private final MissionRepository missionRepository;

    /**
     * 미션 결과 저장
     *
     * @param userId     유저 ID (JWT에서 추출)
     * @param missionId  미션 ID
     * @param dto        요청 DTO (시간, 이탈 횟수, 충돌 횟수 포함)
     * @return 저장된 DroneMissionResult 객체
     */
    public DroneMissionResult saveResult(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("해당 미션을 찾을 수 없습니다."));

        // 단일 미션 타입에 대한 점수 계산
        int score = calculateScore(
                mission.getType(),
                dto.getTotalTime(),
                dto.getDeviationCount(),
                dto.getCollisionCount(),
                dto.getCollectedCoinCount()
        );

        DroneMissionResult result = DroneMissionResult.builder()
                .userId(userId)
                .missionId(missionId)
                .droneId(dto.getDroneId())
                .totalTime(dto.getTotalTime())
                .deviationCount(dto.getDeviationCount())
                .collisionCount(dto.getCollisionCount())
                .score(score)
                .build();

        return resultRepository.save(result);
    }

    /**
     * 미션 유형별 점수 계산
     * - COIN: 빠른 완료 + 이탈/충돌 최소화
     * - OBSTACLE: 이탈 및 충돌 감점이 큼
     * - PHOTO: 시간만 고려
     *
     * @param type            미션 타입 (COIN / OBSTACLE / PHOTO)
     * @param totalTime       총 소요 시간 (초 단위)
     * @param deviationCount  경로 이탈 횟수
     * @param collisionCount  충돌 횟수
     * @return 계산된 점수 (0 ~ 100)
     */
    public int calculateScore(MissionType type, double totalTime, int deviationCount, int collisionCount, Integer collectedCoinCount) {
        switch (type) {
            case COIN:
                int coinCount = collectedCoinCount != null ? collectedCoinCount : 0;
                int baseScore = coinCount * 10;
                int penalty = (int) (totalTime * 0.5);
                return Math.max(0, baseScore - penalty);

            case OBSTACLE:
                return (int) Math.max(100 - (deviationCount * 10 + collisionCount * 10 + totalTime), 0);

            case PHOTO:
                return 0;

            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
    }

    /**
     * 미션 성공 여부 판단
     * @param type 미션 타입
     * @param dto 미션 결과 DTO
     * @return 성공 여부 true/false
     */
    public boolean isMissionSuccess(MissionType type, DroneMissionResultRequestDto dto, Mission mission) {
        switch(type) {
            case COIN:
                return dto.getCollectedCoinCount() != null
                        && dto.getCollectedCoinCount() == mission.getTotalCoinCount();
            case OBSTACLE:
                return dto.getCollisionCount() < 3;
            case PHOTO:
                return dto.getPhotoCaptured() != null && dto.getPhotoCaptured();
            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
    }

}
