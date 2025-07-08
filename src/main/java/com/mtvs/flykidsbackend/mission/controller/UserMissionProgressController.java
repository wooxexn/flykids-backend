package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.mission.dto.UserMissionProgressResponseDto;
import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import com.mtvs.flykidsbackend.mission.service.UserMissionProgressService;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 유저 미션 진행 상태 관리 API 컨트롤러
 * - 인증된 사용자의 미션 진행 현황 조회 및 업데이트 기능 제공
 */
@RestController
@RequestMapping("/api/user-mission-progress")
@RequiredArgsConstructor
@Tag(name = "유저 미션 진행", description = "유저별 미션 단계별 진행 상태 조회 및 업데이트 API")
public class UserMissionProgressController {

    private final UserMissionProgressService progressService;
    private final UserService userService;
    private final MissionService missionService;

    /**
     * 특정 유저, 특정 미션에 대한 모든 단계별 미션 진행 상태 조회
     *
     * @param missionId 조회할 미션 ID
     * @param userDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 해당 유저의 미션 단계별 진행 상태 리스트 반환
     */
    @GetMapping("/missions/{missionId}")
    @Operation(
            summary = "유저 미션 진행 상태 조회",
            description = "로그인한 유저가 특정 미션에 대해 각 단계별 진행 상태를 조회할 수 있는 API입니다. " +
                    "진행 상태 리스트를 반환하여 클라이언트에서 단계별 미션 상태를 보여줍니다."
    )
    public ResponseEntity<List<UserMissionProgressResponseDto>> getProgressList(
            @PathVariable("missionId") Long missionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 인증된 사용자 ID 획득
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증된 사용자가 아닙니다.");
        }
        Long userId = userDetails.getId();

        // 사용자 엔티티 조회, 없으면 404 예외 발생
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."));
        // 미션 엔티티 조회, 없으면 404 예외 발생
        var mission = missionService.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 미션이 존재하지 않습니다."));

        // 유저와 미션에 대한 미션 진행 상태 목록 조회
        List<UserMissionProgress> progressList = progressService.getProgressList(user, mission);

        // 엔티티 -> DTO 변환 후 리스트 생성
        List<UserMissionProgressResponseDto> responseList = progressList.stream()
                .map(UserMissionProgressResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    /**
     * 특정 미션의 특정 단계별 진행 상태 업데이트
     *
     * @param missionId 미션 ID
     * @param missionItemId 미션 아이템(단계) ID
     * @param status 업데이트할 진행 상태 값 (예: "COMPLETED")
     * @param userDetails 인증된 사용자 정보
     * @return 상태 코드 204 (No Content)
     */
    @PostMapping("/missions/{missionId}/items/{missionItemId}")
    @Operation(
            summary = "유저 미션 단계 진행 상태 업데이트",
            description = "특정 미션의 특정 단계에 대한 유저 진행 상태를 업데이트합니다. " +
                    "유저가 미션 단계의 상태를 변경할 때 호출하며, 성공 시 내용 없이 204 상태 코드를 반환합니다."
    )
    public ResponseEntity<Void> updateProgress(
            @PathVariable("missionId") Long missionId,
            @PathVariable("missionItemId") Long missionItemId,
            @RequestParam("status") String status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 인증된 사용자 검증
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증된 사용자가 아닙니다.");
        }
        Long userId = userDetails.getId();

        // 사용자 조회
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."));
        // 미션 조회
        var mission = missionService.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 미션이 존재하지 않습니다."));
        // 미션 아이템 조회
        var missionItem = missionService.findMissionItemById(missionItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 미션 아이템이 존재하지 않습니다."));

        // 진행 상태 업데이트 서비스 호출
        progressService.updateStatus(user, mission, missionItem, status);

        // 업데이트 후 응답은 내용 없음 (204)
        return ResponseEntity.noContent().build();
    }
}
