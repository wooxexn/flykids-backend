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
     * @param dto        요청 DTO (시간, 이탈 횟수 등)
     * @return 저장된 DroneMissionResult 객체
     */
    public DroneMissionResult saveResult(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        // 1. 미션 존재 여부 확인
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("해당 미션을 찾을 수 없습니다."));

        // 2. 점수 계산 (미션 유형별 방식 분기)
        int score = calculateScore(mission.getType(), dto.getTotalTime(), dto.getDeviationCount());

        // 3. 결과 엔티티 생성 및 저장
        DroneMissionResult result = DroneMissionResult.builder()
                .userId(userId)
                .missionId(missionId)
                .droneId(dto.getDroneId())
                .totalTime(dto.getTotalTime())
                .deviationCount(dto.getDeviationCount())
                .score(score)
                .build();

        return resultRepository.save(result);
    }

    /**
     * 점수 계산 로직
     *
     * @param type            미션 타입 (COIN / OBSTACLE / PHOTO)
     * @param totalTime       총 비행 시간
     * @param deviationCount  이탈 횟수
     * @return 계산된 점수 (0~100)
     */
    private int calculateScore(MissionType type, double totalTime, int deviationCount) {
        switch (type) {
            case COIN:
                // 코인 먹기: 빠르게 완료 + 이탈 적으면 높은 점수
                return (int) Math.max(100 - (totalTime * 2 + deviationCount * 5), 0);

            case OBSTACLE:
                // 장애물 피하기: 이탈이 적은 것이 중요
                return (int) Math.max(100 - (deviationCount * 10 + totalTime), 0);

            case PHOTO:
                // 사진 미션: 시간 위주
                return (int) Math.max(100 - totalTime * 3, 0);

            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
    }
}
