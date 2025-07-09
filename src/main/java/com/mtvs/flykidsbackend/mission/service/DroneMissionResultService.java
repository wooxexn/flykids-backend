package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import com.mtvs.flykidsbackend.mission.dto.LeaderboardEntryDto;
import com.mtvs.flykidsbackend.mission.dto.PlayerPerformanceStatsDto;
import com.mtvs.flykidsbackend.mission.dto.MissionHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 드론 미션 결과 관련 비즈니스 로직 처리 서비스
 * - 결과 저장, 이력 조회, 리더보드, 사용자 통계 등을 관리
 */
@Service
@RequiredArgsConstructor
public class DroneMissionResultService {

    private final DroneMissionResultRepository resultRepository;
    private final MissionRepository missionRepository;
    private final ScoreCalculator scoreCalculator;
    private final UserRepository userRepository;

    /**
     * 미션 결과 저장 (단일 미션 결과 저장)
     *
     * @param userId    사용자 ID
     * @param missionId 미션 ID
     * @param dto       수행 결과 요청 DTO (단일 아이템 결과 포함)
     * @return 저장된 결과 엔티티
     */
    @Transactional
    public DroneMissionResult saveMissionResult(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new NoSuchElementException("해당 미션을 찾을 수 없습니다."));

        // 점수 계산 및 성공 여부 판정 시 DTO 전체 전달
        int score = scoreCalculator.calculateScore(mission.getType(), dto);
        boolean success = scoreCalculator.isMissionSuccess(mission.getType(), dto, mission);

        // 단일 MissionItemResult 필드 사용
        DroneMissionResultRequestDto.MissionItemResult item = dto.getItemResult();

        DroneMissionResult result = DroneMissionResult.builder()
                .userId(userId)
                .mission(mission)
                .droneId(dto.getDroneId())
                .totalTime(item.getTotalTime())
                .deviationCount(item.getDeviationCount())
                .collisionCount(item.getCollisionCount())
                .score(score)
                .status(success ? MissionResultStatus.SUCCESS : MissionResultStatus.FAIL)
                .build();

        return resultRepository.save(result);
    }


    /**
     * 미션 성공 여부 판단
     * - 단일 미션 수행 결과를 바탕으로 성공/실패를 결정한다.
     *
     * @param type    미션 유형 (COIN, OBSTACLE, PHOTO)
     * @param item    단일 미션 아이템 수행 결과 DTO
     * @param mission DB에 저장된 미션 정보 (성공 기준 데이터 포함)
     * @return 성공 여부 (true: 성공, false: 실패)
     * @throws IllegalArgumentException 지원하지 않는 미션 유형일 경우 발생
     */
    public boolean isMissionSuccess(MissionType type, DroneMissionResultRequestDto.MissionItemResult item, Mission mission) {
        return switch (type) {
            case COIN -> item.getCollectedCoinCount() != null &&
                    item.getCollectedCoinCount().equals(mission.getTotalCoinCount());
            case OBSTACLE -> item.getCollisionCount() < 3;
            case PHOTO -> Boolean.TRUE.equals(item.getPhotoCaptured());
            default -> throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");
        };
    }

    /**
     * 사용자 미션 수행 이력 전체 조회
     *
     * @param userId 사용자 ID
     * @return 미션 이력 DTO 리스트
     */
    public List<MissionHistoryResponseDto> getMyMissionHistory(Long userId) {
        return resultRepository
                .findByUserIdAndStatusNot(userId, MissionResultStatus.ABORT)
                .stream()
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
                .collect(Collectors.toList());
    }

    /**
     * 특정 미션의 리더보드 TOP10 조회
     *
     * @param missionId 미션 ID
     * @return 상위 10명의 랭킹 DTO 리스트
     */
    public List<LeaderboardEntryDto> getTopRankers(Long missionId) {
        List<DroneMissionResult> results =
                resultRepository.findTop10ByMission_IdAndStatusOrderByScoreDesc(
                        missionId, MissionResultStatus.SUCCESS);

        int[] rank = {1};
        return results.stream()
                .map(r -> LeaderboardEntryDto.builder()
                        .rank(rank[0]++)
                        .nickname(userRepository.findNicknameById(r.getUserId()))
                        .score(r.getScore())
                        .totalTime(r.getTotalTime())
                        .completedAt(r.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 전체 미션 통계 조회
     *
     * @param userId 사용자 ID
     * @return 통계 DTO
     */
    public PlayerPerformanceStatsDto getPlayerStats(Long userId) {
        List<DroneMissionResult> results =
                resultRepository.findByUserIdAndStatusNot(userId, MissionResultStatus.ABORT);
        int totalAttempts = results.size();

        // 같은 mission.id를 기준으로 COIN/OBSTACLE/PHOTO 모두 success=true일 때 1세트로 간주
        Map<Long, Long> successCountByMission =
                results.stream()
                        .filter(r -> r.getStatus() == MissionResultStatus.SUCCESS)
                        .collect(Collectors.groupingBy(r -> r.getMission().getId(), Collectors.counting()));

        int successfulSets = (int) successCountByMission.values().stream()
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

    /**
     * 미션 중단 처리
     * 유저가 미션을 중단(포기)했을 때 기본값으로 결과 데이터를 저장한다.
     *
     * @param missionId 중단된 미션의 ID
     * @param userId    미션을 중단한 사용자 ID
     * @param droneId   사용한 드론의 ID
     */
    @Transactional
    public void abortMission(Long missionId, Long userId, String droneId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        DroneMissionResult result = DroneMissionResult.builder()
                .userId(userId)
                .droneId(droneId)
                .totalTime(0)
                .deviationCount(0)
                .collisionCount(0)
                .score(0)
                .status(MissionResultStatus.ABORT)
                .mission(mission)
                .build();

        resultRepository.save(result);
    }

    /**
     * 실패한 미션 결과들을 재도전 가능 상태로 초기화한다.
     * - 유저가 특정 미션을 다시 시도할 때, 실패 상태(FAIL)인 결과를
     *   NOT_ATTEMPTED 상태로 변경하여 재도전할 수 있도록 한다.
     *
     * @param userId    재도전할 유저의 ID
     * @param missionId 재도전할 미션의 ID
     */
    @Transactional
    public void retryFailedMission(Long userId, Long missionId) {
        List<DroneMissionResult> failedResults = resultRepository.findByUserIdAndMissionIdAndStatus(
                userId,
                missionId,
                MissionResultStatus.FAIL
        );

        for (DroneMissionResult result : failedResults) {
            result.setStatus(MissionResultStatus.NOT_ATTEMPTED);
            resultRepository.save(result);
        }
    }
}
