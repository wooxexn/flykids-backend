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

    /**
     * 로그인한 유저의 미션 수행 이력 전체 조회
     *
     * @param userId 유저 ID
     * @return 유저의 미션 결과 이력 리스트
     */
    public List<MissionHistoryResponseDto> getMyMissionHistory(Long userId) {
        List<DroneMissionResult> results = resultRepository.findByUserId(userId);

        return results.stream()
                .map(result -> MissionHistoryResponseDto.builder()
                        .missionId(result.getMissionId())
                        .missionName(result.getMission() != null ? result.getMission().getTitle() : "알 수 없음")
                        .missionType(result.getMission() != null ? result.getMission().getType() : null)
                        .score(result.getScore())
                        .totalTime(result.getTotalTime())
                        .deviationCount(result.getDeviationCount())
                        .collisionCount(result.getCollisionCount())
                        .completedAt(result.getCompletedAt())
                        .build())
                .toList();
    }

    /**
     * 특정 미션의 점수 상위 10명을 리더보드 형태로 반환한다.
     *
     * <p>처리 순서
     * <ol>
     *   <li>{@link DroneMissionResultRepository#findTop10ByMissionIdOrderByScoreDesc(Long)}
     *       로 결과를 가져온다.</li>
     *   <li>userId ➜ nickname 변환은 {@link UserRepository#findNicknameById(Long)} 로
     *       활성(Active) 사용자만 대상으로 조회한다.</li>
     *   <li>Stream API로 DTO 변환 및 순위(rank) 부여 후 반환한다.</li>
     * </ol>
     *
     * @param missionId 조회할 미션 ID
     * @return 닉네임·점수·소요 시간·완료 시각을 포함한 TOP10 리스트
     */
    public List<LeaderboardEntryDto> getTopRankers(Long missionId) {
        List<DroneMissionResult> results =
                resultRepository.findTop10ByMissionIdOrderByScoreDesc(missionId);

        int[] rank = {1}; // 1부터 시작하는 순위 카운터

        return results.stream()
                .map(r -> LeaderboardEntryDto.builder()
                        .rank(rank[0]++)                           // 순위 증가
                        .nickname(userRepository.findNicknameById(r.getUserId()))
                        .score(r.getScore())
                        .totalTime(r.getTotalTime())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();
    }

    /**
     * 로그인한 유저의 통계 정보 계산
     *
     * <p>계산 항목
     * <ul>
     *   <li><b>totalAttempts</b>  : 하위 미션(COIN/OBSTACLE/PHOTO) 시도 횟수</li>
     *   <li><b>successfulSets</b> : 3종 미션을 모두 성공한 세트 수</li>
     *   <li><b>averageScore</b>   : 평균 점수</li>
     *   <li><b>totalFlightTime</b>: 총 비행 시간(초)</li>
     * </ul>
     *
     * @param userId 로그인한 유저 ID
     * @return {@link PlayerPerformanceStatsDto}
     */
    public PlayerPerformanceStatsDto getPlayerStats(Long userId) {

        /* 전체 기록 조회 */
        List<DroneMissionResult> results = resultRepository.findByUserId(userId);

        /* 1) 시도 횟수 */
        int totalAttempts = results.size();

    /* 2) 최종 승리 세트 계산
       - 같은 missionId 를 가진 결과 3개(COIN·OBSTACLE·PHOTO)가 모두 success=true 일 때 1세트로 인정
     */
        Map<Long, Long> successCountByMission =
                results.stream()
                        .filter(DroneMissionResult::isSuccess)
                        .collect(Collectors.groupingBy(DroneMissionResult::getMissionId,
                                Collectors.counting()));

        int successfulSets =
                (int) successCountByMission.values().stream()
                        .filter(c -> c >= 3)   // 3종 모두 성공한 missionId
                        .count();

        /* 3) 평균 점수, 총 비행 시간 (JPQL 한 번씩 호출) */
        double averageScore   =
                Optional.ofNullable(resultRepository.findAverageScoreByUserId(userId)).orElse(0.0);
        double totalFlightSec =
                Optional.ofNullable(resultRepository.findTotalFlightTimeByUserId(userId)).orElse(0.0);

        /* DTO 빌드 & 반환 */
        return PlayerPerformanceStatsDto.builder()
                .totalAttempts(totalAttempts)
                .successfulSets(successfulSets)
                .averageScore(Math.round(averageScore * 10.0) / 10.0)   // 소수 1자리 반올림
                .totalFlightTime(Math.round(totalFlightSec * 10.0) / 10.0)
                .build();
    }
}
