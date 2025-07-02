package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto.MissionItemResult;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionItemRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DroneMissionResultService {

    private final DroneMissionResultRepository resultRepository;
    private final MissionRepository missionRepository;
    private final MissionItemRepository missionItemRepository;

    /**
     * 복합 미션 결과 저장
     * - 미션 아이템별 결과를 각각 처리하여 저장
     * - 각 아이템 점수 계산 및 성공 여부 판단
     *
     * @param userId    유저 ID (JWT에서 추출)
     * @param missionId 미션 ID (복합 미션 단위)
     * @param dto       미션 아이템별 결과 리스트 포함 DTO
     * @return 저장된 DroneMissionResult 리스트
     */
    public List<DroneMissionResult> saveComplexMissionResult(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("해당 미션을 찾을 수 없습니다."));

        // missionItems 가져오기
        List<MissionItem> missionItems = missionItemRepository.findByMissionId(missionId);

        // 클라이언트에서 받은 itemResults와 DB missionItems 매칭 후 처리
        List<DroneMissionResult> results = dto.getItemResults().stream()
                .map(itemResult -> {
                    MissionItem missionItem = missionItems.stream()
                            .filter(mi -> mi.getType() == itemResult.getMissionType())
                            .findFirst()
                            .orElseThrow(() -> new NoSuchElementException("해당 미션 아이템을 찾을 수 없습니다."));

                    int score = calculateScore(
                            missionItem.getType(),
                            itemResult.getTotalTime(),
                            itemResult.getDeviationCount(),
                            itemResult.getCollisionCount(),
                            itemResult.getCollectedCoinCount()
                    );

                    boolean success = isMissionSuccess(missionItem.getType(), itemResult, missionItem);

                    DroneMissionResult result = DroneMissionResult.builder()
                            .userId(userId)
                            .missionId(missionId)
                            .droneId(dto.getDroneId())
                            .totalTime(itemResult.getTotalTime())
                            .deviationCount(itemResult.getDeviationCount())
                            .collisionCount(itemResult.getCollisionCount())
                            .score(score)
                            .success(success)
                            .build();

                    return resultRepository.save(result);
                })
                .collect(Collectors.toList());

        return results;
    }

    /**
     * 미션 유형별 점수 계산
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
     * 미션 성공 여부 판단 (복합 미션 아이템 기준)
     */
    public boolean isMissionSuccess(MissionType type, MissionItemResult dto, MissionItem missionItem) {
        switch(type) {
            case COIN:
                return dto.getCollectedCoinCount() != null
                        && dto.getCollectedCoinCount() == (missionItem.getTotalCoinCount() != null ? missionItem.getTotalCoinCount() : 0);
            case OBSTACLE:
                return dto.getCollisionCount() < 3;
            case PHOTO:
                return dto.getPhotoCaptured() != null && dto.getPhotoCaptured();
            default:
                throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }
    }
}
