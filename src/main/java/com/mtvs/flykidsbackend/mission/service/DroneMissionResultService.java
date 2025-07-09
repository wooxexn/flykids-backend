package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.*;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionItemResult;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto.MissionItemResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.*;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
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
    private final MissionItemRepository missionItemRepository;
    private final ScoreCalculator scoreCalculator;
    private final UserRepository userRepository;
    private final DroneMissionItemResultRepository itemResultRepository;

    /**
     * 미션 결과 저장
     * <p>복합 미션 요청을 받아 각 하위 미션(COIN/OBSTACLE/PHOTO)의 결과를 저장한다.</p>
     *
     * @param userId    사용자 ID
     * @param missionId 미션 ID
     * @param dto       수행 결과 요청 DTO
     * @return 저장된 결과 리스트
     * @throws NoSuchElementException 미션 또는 미션 아이템을 찾을 수 없는 경우
     */
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
                    MissionResultStatus status = success ? MissionResultStatus.SUCCESS : MissionResultStatus.FAIL;  // ✅ 선언 추가

                    return resultRepository.save(
                            DroneMissionResult.builder()
                                    .userId(userId)
                                    .mission(mission)
                                    .droneId(dto.getDroneId())
                                    .totalTime(itemResult.getTotalTime())
                                    .deviationCount(itemResult.getDeviationCount())
                                    .collisionCount(itemResult.getCollisionCount())
                                    .score(score)
                                    .status(status)
                                    .build()
                    );
                })
                .toList();
    }

    /**
     * 미션 성공 여부 판단
     *
     * @param type        미션 타입
     * @param dto         사용자 제출 결과
     * @param missionItem 기준 미션 정보
     * @return 성공 여부
     */
    public boolean isMissionSuccess(MissionType type,
                                    MissionItemResult dto,
                                    MissionItem missionItem) {

        if (type == null)
            throw new IllegalArgumentException("지원하지 않는 미션 유형입니다.");

        return switch (type) {
            case COIN -> dto.getCollectedCoinCount() != null
                    && dto.getCollectedCoinCount()
                    == Optional.ofNullable(missionItem.getTotalCoinCount()).orElse(0);
            case OBSTACLE -> dto.getCollisionCount() < 3;
            case PHOTO -> Boolean.TRUE.equals(dto.getPhotoCaptured());
            default -> throw new IllegalArgumentException("지원하지 않는 미션 유형입니다: " + type);
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
                .toList();
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
                .toList();
    }

    /**
     * 사용자 전체 미션 통계 조회
     * <p>성공 세트 수, 평균 점수, 총 비행 시간 등을 포함한다.</p>
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
     * @throws IllegalArgumentException 미션이 존재하지 않을 경우 예외 발생
     */
    @Transactional
    public void abortMission(Long missionId, Long userId, String droneId) {

        // 1. 미션 존재 여부 확인
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        // 2. 중단된 결과 생성 (성공 여부 false, 점수/시간/이탈/충돌 모두 0으로 초기화)
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

        // 3. 결과 저장
        resultRepository.save(result);
    }

    /**
     * 실패한 단계 재도전 처리 메서드
     * <p>
     * - 유저가 특정 미션을 다시 시도할 때, 기존에 실패한 단계만 초기화하여
     * NOT_ATTEMPTED 상태로 변경한다.
     * - 이미 성공한 단계는 그대로 유지하여, 실패한 단계부터 이어서 재도전 가능하게 한다.
     * </p>
     *
     * @param userId    재도전할 유저의 ID
     * @param missionId 재도전할 미션의 ID
     */
    @Transactional
    public void retryFailedItems(Long userId, Long missionId) {

        // 1. 해당 유저가 수행한 미션 중, 실패(Failure)한 단계들만 조회
        List<DroneMissionItemResult> failedItems =
                itemResultRepository.findByUserIdAndMissionIdAndStatus(
                        userId,
                        missionId,
                        MissionResultStatus.FAIL
                );

        // 2. 각 실패한 단계의 상태를 'NOT_ATTEMPTED'로 변경하여 재도전 가능하게 함
        for (DroneMissionItemResult item : failedItems) {
            item.setStatus(MissionResultStatus.NOT_ATTEMPTED);
            itemResultRepository.save(item);  // 상태 갱신
        }
    }
}

