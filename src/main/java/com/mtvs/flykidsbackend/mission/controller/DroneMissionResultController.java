package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.mission.dto.*;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.service.DroneMissionResultService;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

/**
 * 미션 결과 관련 API 컨트롤러
 * - 미션 완료 후 점수 계산 및 결과 저장, TTS 응답 포함
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(
        name = "Mission",
        description = "드론 미션 시작, 완료, 결과 조회, 통계 등 미션 전반 관리 API"
)
public class DroneMissionResultController {

    private final MissionService missionService;
    private final DroneMissionResultService droneMissionResultService;


    /**
     * 미션 수행 결과 저장 및 피드백 응답 API
     *
     * @param missionId   수행한 미션 ID
     * @param requestDto  미션 결과 정보 (총 시간, 이탈 횟수, 드론 ID 등)
     * @param authentication  현재 인증된 사용자 정보가 담긴 객체
     * @return 점수 및 TTS 피드백 메시지를 포함한 응답
     */
    @PostMapping("/{missionId}/complete")
    @Operation(summary = "미션 완료 처리", description = "결과 저장 후 점수 및 TTS 피드백 메시지를 반환합니다.")
    public ResponseEntity<MissionCompleteResponseDto> completeMission(
            @PathVariable("missionId") Long missionId,
            @RequestBody DroneMissionResultRequestDto requestDto,
            Authentication authentication
    ) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            MissionCompleteResponseDto response =
                    missionService.completeMission(userId, missionId, requestDto);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 로그인한 유저의 미션 수행 이력 조회 API
     *
     * - JWT 인증을 통해 로그인한 유저 정보를 확인
     * - 해당 유저가 완료한 모든 미션 결과를 조회하여 응답
     * - 미션 이름, 점수, 소요 시간, 이탈/충돌 횟수, 완료 시간 포함
     *
     * @param userDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 유저의 미션 수행 기록 리스트
     */
    @GetMapping("/users/me/history")
    @Operation(summary = "내 미션 수행 이력 조회", description = "로그인한 사용자의 미션 결과 기록 목록을 반환합니다.")
    public ResponseEntity<List<MissionHistoryResponseDto>> getMyMissionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        List<MissionHistoryResponseDto> history = droneMissionResultService.getMyMissionHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * 특정 미션 리더보드 조회 API
     *
     * - 미션 ID로 필터링하여 점수 상위 10명의 유저 조회
     * - 닉네임, 점수, 소요 시간, 완료 시간 포함
     *
     * @param missionId 조회할 미션 ID
     * @return 리더보드 정보 리스트
     */
    @GetMapping("/leaderboard")
    @Operation(summary = "리더보드 조회", description = "미션 ID 기준으로 상위 점수 유저 TOP10을 조회합니다.")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(
            @RequestParam Long missionId
    ) {
        List<LeaderboardEntryDto> leaderboard = droneMissionResultService.getTopRankers(missionId);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * 로그인한 사용자의 개인 미션 통계 조회 API
     *
     * <p>통계를 통해 유저의 학습 이력 및 성과를 정량적으로 파악할 수 있다.
     *
     * <ul>
     *   <li><b>총 시도 횟수</b>: COIN/OBSTACLE/PHOTO 미션 시도 총합</li>
     *   <li><b>세트 성공 횟수</b>: 같은 미션 ID 내에서 3종 모두 성공한 횟수</li>
     *   <li><b>평균 점수</b>: 유저의 전체 평균 점수</li>
     *   <li><b>총 비행 시간</b>: 모든 미션에서의 비행 시간 합계 (초 단위)</li>
     * </ul>
     *
     * @param userDetails 인증된 사용자 정보 (Spring Security)
     * @return 유저 통계 정보 {@link PlayerPerformanceStatsDto}
     */
    @GetMapping("/users/me/stats")
    @Operation(summary = "개인 통계 조회",
            description = "총 시도, 세트 승리 횟수, 평균 점수, 총 비행 시간을 반환합니다.")
    public ResponseEntity<PlayerPerformanceStatsDto> getMyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                droneMissionResultService.getPlayerStats(userDetails.getId()));
    }

    /**
     * 고정된 시작 미션 조회 API
     *
     * - 게임 시작 시 항상 동일한 복합 미션을 할당하기 위한 용도
     * - MVP 단계에서는 무작위 또는 추천 기반이 아닌, 특정 ID의 미션(예: 22번)을 고정 제공
     * - 추후 추천 알고리즘 또는 사용자 맞춤 로직으로 확장 가능
     *
     * <p>예시: Unity에서 사용자가 입장할 때 호출하여 첫 미션 정보를 수신함</p>
     *
     * @return MissionResponseDto - 고정된 시작 미션 정보
     */
    @Operation(summary = "게임 시작용 미션", description = "플레이어 입장 시 서버가 고정된 복합 미션을 할당합니다.")
    @GetMapping("/starting")
    public ResponseEntity<MissionResponseDto> getStartingMission() {
        Mission mission = missionService.getMissionEntity(22L);
        return ResponseEntity.ok(MissionResponseDto.from(mission));
    }

    /**
     * [POST] 미션 중단(포기) 처리
     * - 진행 중인 복합‧단일 미션을 사용자가 포기했을 때 호출
     * - DroneMissionResult.status = ABORT 로 저장
     *
     * @param id           중단할 미션 ID
     * @param userDetails  JWT 인증 정보
     * @return 중단 완료 메시지
     */
    @Operation(summary = "미션 중단(포기)", description = "미션을 중단(포기)합니다.")
    @PostMapping("/{id}/abort")
    public ResponseEntity<Map<String, String>> abortMission(
            @PathVariable("id") Long id,
            @RequestBody DroneAbortRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        droneMissionResultService.abortMission(id, userDetails.getId(), requestDto.getDroneId());

        return ResponseEntity.ok(
                Map.of("message", "미션을 중단했습니다. 다음에 다시 도전해보세요!")
        );
    }
}