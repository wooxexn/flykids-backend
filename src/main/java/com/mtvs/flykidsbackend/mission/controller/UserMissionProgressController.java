package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.mission.dto.UserMissionProgressResponseDto;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import com.mtvs.flykidsbackend.mission.service.UserMissionProgressService;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.model.UserMissionStatus;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user-mission-progress")
@RequiredArgsConstructor
@Tag(name = "유저 미션 진행", description = "단일 미션 단위 진행 상태 조회 및 업데이트 API")
public class UserMissionProgressController {

    private final UserMissionProgressService progressService;
    private final UserService userService;
    private final MissionService missionService;

    /**
     * 특정 유저, 특정 미션에 대한 진행 상태 조회 (단일 미션 기준)
     */
    @GetMapping("/missions/{missionId}")
    @Operation(summary = "유저 미션 진행 상태 조회", description = "단일 미션에 대한 로그인 유저 진행 상태 조회")
    public ResponseEntity<UserMissionProgressResponseDto> getProgress(
            @PathVariable Long missionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."));
        var mission = missionService.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 미션이 존재하지 않습니다."));

        return progressService.getProgress(user, mission)
                .map(progress -> ResponseEntity.ok(UserMissionProgressResponseDto.from(progress)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 특정 유저, 특정 미션에 대한 진행 상태 업데이트 (단일 미션 기준)
     */
    @PostMapping("/missions/{missionId}")
    @Operation(summary = "유저 미션 진행 상태 업데이트", description = "단일 미션에 대한 로그인 유저 진행 상태 업데이트")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long missionId,
            @RequestParam("status") String status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."));

        Mission mission = missionService.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 미션이 존재하지 않습니다."));

        try {
            UserMissionStatus enumStatus = UserMissionStatus.valueOf(status.toUpperCase());
            progressService.updateStatus(user, mission, enumStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 상태 값입니다: " + status);
        }

        return ResponseEntity.noContent().build();
    }

}
