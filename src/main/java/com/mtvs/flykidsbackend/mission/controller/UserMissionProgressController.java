package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.mission.dto.UserMissionProgressResponseDto;
import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import com.mtvs.flykidsbackend.mission.service.UserMissionProgressService;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 유저 미션 진행 상태 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/user-mission-progress")
@RequiredArgsConstructor
@Tag(name = "UserMissionProgress", description = "유저 미션 진행 상태 API")
public class UserMissionProgressController {

    private final UserMissionProgressService progressService;
    private final UserService userService;
    private final MissionService missionService;

    /**
     * 특정 유저, 미션에 대한 모든 단계별 진행 상태 조회
     */
    @GetMapping("/missions/{missionId}")
    @Operation(summary = "유저 미션 진행 상태 조회", description = "특정 미션에 대한 유저의 단계별 진행 상태를 조회합니다.")
    public ResponseEntity<List<UserMissionProgressResponseDto>> getProgressList(
            @PathVariable Long missionId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));
        var mission = missionService.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        List<UserMissionProgress> progressList = progressService.getProgressList(user, mission);

        List<UserMissionProgressResponseDto> responseList = progressList.stream()
                .map(UserMissionProgressResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    /**
     * 미션 단계별 진행 상태 업데이트
     */
    @PostMapping("/missions/{missionId}/items/{missionItemId}")
    @Operation(summary = "유저 미션 단계 진행 상태 업데이트", description = "특정 미션 단계의 진행 상태를 업데이트합니다.")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long missionId,
            @PathVariable Long missionItemId,
            @RequestParam String status,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        var mission = missionService.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        var missionItem = missionService.findMissionItemById(missionItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션 아이템이 존재하지 않습니다."));

        progressService.updateStatus(user, mission, missionItem, status);

        return ResponseEntity.noContent().build();
    }
}
