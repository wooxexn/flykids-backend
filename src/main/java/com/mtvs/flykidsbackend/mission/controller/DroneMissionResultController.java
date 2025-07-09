package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.mission.dto.*;
import com.mtvs.flykidsbackend.mission.service.DroneMissionResultService;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 드론 미션 결과 관련 API 컨트롤러 (단일 미션 기준)
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Mission", description = "드론 미션 단일 미션 관련 API")
public class DroneMissionResultController {

    private final MissionService missionService;
    private final DroneMissionResultService droneMissionResultService;

    /**
     * 미션 완료 처리 API (단일 미션 결과 저장 및 피드백 반환)
     */
    @PostMapping("/{missionId}/complete")
    @Operation(
            summary = "미션 완료 처리",
            description = "단일 미션 수행 결과 저장 및 점수, 성공 여부, 피드백 메시지를 반환합니다."
    )
    public ResponseEntity<MissionCompleteResponseDto> completeMission(
            @PathVariable Long missionId,
            @RequestBody DroneMissionResultRequestDto requestDto,
            Authentication authentication) {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            MissionCompleteResponseDto response =
                    missionService.completeMission(userId, missionId, requestDto);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 로그인한 유저 미션 수행 이력 조회 API
     */
    @GetMapping("/users/me/history")
    @Operation(
            summary = "유저 미션 수행 이력 조회",
            description = "현재 로그인한 유저의 미션 결과 이력을 조회합니다."
    )
    public ResponseEntity<List<MissionHistoryResponseDto>> getMyMissionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MissionHistoryResponseDto> history =
                droneMissionResultService.getMyMissionHistory(userDetails.getId());

        return ResponseEntity.ok(history);
    }

    /**
     * 특정 미션 리더보드 조회 API (단일 미션 기준)
     */
    @GetMapping("/leaderboard")
    @Operation(
            summary = "미션 리더보드 조회",
            description = "지정 미션의 상위 10명 랭킹 정보를 조회합니다."
    )
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(
            @RequestParam Long missionId) {

        List<LeaderboardEntryDto> leaderboard = droneMissionResultService.getTopRankers(missionId);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * 로그인한 유저 개인 미션 통계 조회 API
     */
    @GetMapping("/users/me/stats")
    @Operation(
            summary = "개인 미션 통계 조회",
            description = "로그인한 유저의 미션 시도 횟수, 성공 세트 수, 평균 점수, 총 비행 시간을 조회합니다."
    )
    public ResponseEntity<PlayerPerformanceStatsDto> getMyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PlayerPerformanceStatsDto stats = droneMissionResultService.getPlayerStats(userDetails.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * 미션 중단(포기) 처리 API (단일 미션 기준)
     */
    @PostMapping("/{missionId}/abort")
    @Operation(
            summary = "미션 중단 처리",
            description = "사용자가 미션 수행을 중단하거나 포기할 때 상태를 ABORT로 저장합니다."
    )
    public ResponseEntity<Map<String, String>> abortMission(
            @PathVariable Long missionId,
            @RequestBody DroneAbortRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        droneMissionResultService.abortMission(missionId, userDetails.getId(), requestDto.getDroneId());

        return ResponseEntity.ok(Map.of("message", "미션을 중단했습니다. 다음에 다시 도전해보세요!"));
    }

    /**
     * 실패한 미션 재도전 초기화 API (단일 미션 기준)
     */
    @PostMapping("/{missionId}/retry")
    @Operation(
            summary = "실패 미션 재도전 초기화",
            description = "실패한 미션 결과 상태를 초기화하여 재도전할 수 있도록 설정합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Map<String, String>> retryFailedMission(
            @PathVariable Long missionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        droneMissionResultService.retryFailedMission(userDetails.getId(), missionId);

        return ResponseEntity.ok(Map.of("message", "실패한 미션을 다시 도전할 수 있도록 초기화했습니다."));
    }
}
