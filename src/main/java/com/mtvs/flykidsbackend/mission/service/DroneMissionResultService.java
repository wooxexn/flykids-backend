package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto.MissionItemResult;
import com.mtvs.flykidsbackend.mission.dto.LeaderboardEntryDto;
import com.mtvs.flykidsbackend.mission.dto.MissionHistoryResponseDto;
import com.mtvs.flykidsbackend.mission.dto.PlayerPerformanceStatsDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionItemRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DroneMissionResultService {

    private final DroneMissionResultRepository resultRepository;
    private final MissionRepository missionRepository;
    private final MissionItemRepository missionItemRepository;
    private final ScoreCalculator scoreCalculator;
    private final UserRepository userRepository;

    /* ------------------------------ 결과 저장 ------------------------------ */

    public List<DroneMissionResult> saveComplexMissionResult(Long userId,
                                                             Long missionId,
                                                             DroneMissionResultRequestDto dto) {

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("해당 미션을 찾을 수 없습니다."));

        List<MissionItem> missionItems = missionItemRepository.findByMissionId(missionId);

        return dto.getItemResults().stream()
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
                            Optional.ofNullable(itemResult.getCollectedCoinCount()).orElse(0)
                    );

                    boolean success = isMissionSuccess(missionItem.getType(), itemResult, missionItem);

                    /* missionId 대신 mission 객체 자체를 저장 */
                    DroneMissionResult result = DroneMissionResult.builder()
                            .userId(userId)
                            .mission(mission)
                            .droneId(dto.getDroneId())
                            .totalTime(itemResult.getTotalTime())
                            .deviationCount(itemResult.getDeviationCount())
                            .collisionCount(itemResult.getCollisionCount())
                            .score(score)
                            .success(success)
                            .build();

                    return resultRepository.save(result);
                })
                .toList();
    }

    /* --------------------------- 미션 성공 판단 --------------------------- */

    public boolean isMissionSuccess(MissionType type,
                                    MissionItemResult dto,
                                    MissionItem missionItem) {

        if (type == null) throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");

        return switch (type) {
            case COIN -> dto.getCollectedCoinCount() != null
                    && dto.getCollectedCoinCount()
                    == Optional.ofNullable(missionItem.getTotalCoinCount()).orElse(0);
            case OBSTACLE -> dto.getCollisionCount() < 3;
            case PHOTO -> Boolean.TRUE.equals(dto.getPhotoCaptured());
            default -> throw new IllegalArgumentException("지원하지 않는 미션 유형입니다: " + type);
        };
    }

    /* ---------------------------- 이력 조회 ---------------------------- */

    public List<MissionHistoryResponseDto> getMyMissionHistory(Long userId) {
        return resultRepository.findByUserId(userId).stream()
                .map(r -> MissionHistoryResponseDto.builder()
                        .missionId(r.getMission().getId())
                        .missionName(r.getMission().getTitle())
                        .missionType(r.getMission().getType())
                        .score(r.getScore())
                        .totalTime(r.getTotalTime())
                        .deviationCount(r.getDeviationCount())
                        .collisionCount(r.getCollisionCount())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();
    }

    /* -------------------------- 리더보드 조회 -------------------------- */

    public List<LeaderboardEntryDto> getTopRankers(Long missionId) {

        /* 메서드 명 변경: Mission 엔티티 경로 사용 */
        List<DroneMissionResult> results =
                resultRepository.findTop10ByMission_IdOrderByScoreDesc(missionId);

        int[] rank = {1};

        return results.stream()
                .map(r -> LeaderboardEntryDto.builder()
                        .rank(rank[0]++)
                        .nickname(userRepository.findNicknameById(r.getUserId()))
                        .score(r.getScore())
                        .totalTime(r.getTotalTime())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();
    }

    /* ----------------------------- 통계 ----------------------------- */

    public PlayerPerformanceStatsDto getPlayerStats(Long userId) {

        List<DroneMissionResult> results = resultRepository.findByUserId(userId);

        int totalAttempts = results.size();

        /* 같은 mission.id 를 가진 결과 3개(COIN·OBSTACLE·PHOTO) 모두 success=true → 1세트 */
        Map<Long, Long> successCountByMission =
                results.stream()
                        .filter(DroneMissionResult::isSuccess)
                        .collect(Collectors.groupingBy(r -> r.getMission().getId(),
                                Collectors.counting()));

        int successfulSets =
                (int) successCountByMission.values().stream()
                        .filter(c -> c >= 3)
                        .count();

        double averageScore =
                Optional.ofNullable(resultRepository.findAverageScoreByUserId(userId)).orElse(0.0);

        double totalFlightSec =
                Optional.ofNullable(resultRepository.findTotalFlightTimeByUserId(userId)).orElse(0.0);

        return PlayerPerformanceStatsDto.builder()
                .totalAttempts(totalAttempts)
                .successfulSets(successfulSets)
                .averageScore(Math.round(averageScore * 10) / 10.0)
                .totalFlightTime(Math.round(totalFlightSec * 10) / 10.0)
                .build();
    }
}
