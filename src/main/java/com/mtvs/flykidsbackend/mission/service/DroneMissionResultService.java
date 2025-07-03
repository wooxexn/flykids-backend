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
    private final ScoreCalculator scoreCalculator;

    public List<DroneMissionResult> saveComplexMissionResult(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("해당 미션을 찾을 수 없습니다."));

        List<MissionItem> missionItems = missionItemRepository.findByMissionId(missionId);

        List<DroneMissionResult> results = dto.getItemResults().stream()
                .map(itemResult -> {
                    MissionItem missionItem = missionItems.stream()
                            .filter(mi -> mi.getType() == itemResult.getMissionType())
                            .findFirst()
                            .orElseThrow(() -> new NoSuchElementException("해당 미션 아이템을 찾을 수 없습니다."));

                    int score = scoreCalculator.calculateScore(
                            missionItem.getType(),
                            itemResult.getTotalTime(),
                            itemResult.getDeviationCount(),
                            itemResult.getCollisionCount(),
                            itemResult.getCollectedCoinCount() != null ? itemResult.getCollectedCoinCount() : 0
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

    // 미션 성공 판단 로직
    public boolean isMissionSuccess(MissionType type, MissionItemResult dto, MissionItem missionItem) {
        if (type == null) {
            throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        }

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
