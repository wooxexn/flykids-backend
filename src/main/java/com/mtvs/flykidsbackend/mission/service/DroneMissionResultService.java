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
                dto.getCollisionCount()
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
    public int calculateScore(MissionType type, double totalTime, int deviationCount, int collisionCount) {
        switch (type) {
            case COIN:
                // 코인 미션: 시간 + 이탈 + 충돌 모두 감점 요소
                return (int) Math.max(100 - (totalTime * 2 + deviationCount * 5 + collisionCount * 10), 0);

            case OBSTACLE:
                // 장애물 미션: 이탈과 충돌 감점이 큼
                return (int) Math.max(100 - (deviationCount * 10 + collisionCount * 10 + totalTime), 0);

            case PHOTO:
                // 사진 미션: 시간 중심, 충돌은 반영하지 않음
                return (int) Math.max(100 - totalTime * 3, 0);

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
    public boolean isMissionSuccess(MissionType type, DroneMissionResultRequestDto dto) {
        switch(type) {
            case COIN:
                // 예: 코인 미션은 최소 10개 이상 수집해야 성공
                return dto.getCollectedCoinCount() != null && dto.getCollectedCoinCount() >= 10;
            case OBSTACLE:
                // 예: 장애물 미션은 충돌 3회 미만이어야 성공
                return dto.getCollisionCount() < 3;
            case PHOTO:
                // 예: 사진 촬영 성공해야 미션 성공
                return dto.getPhotoCaptured() != null && dto.getPhotoCaptured();
            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
    }

}
