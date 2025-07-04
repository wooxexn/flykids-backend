package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 미션 관리 API 컨트롤러
 * - 미션 등록, 수정, 삭제, 조회 기능을 제공한다
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Mission", description = "미션 관리 API")
public class MissionController {

    private final MissionService missionService;

    /**
     * [POST] 미션 등록
     * - 새로운 미션을 등록한다.
     * - 미션 제목, 제한 시간, 타입(COIN/OBSTACLE/PHOTO)을 포함한다.
     *
     * @param requestDto 등록 요청 DTO
     * @return 등록된 미션 정보
     */
    @Operation(summary = "미션 등록", description = "새로운 미션을 등록합니다.")
    @PostMapping
    public ResponseEntity<MissionResponseDto> createMission(
            @RequestBody MissionRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getId();

        // userId를 기반으로 권한 체크하거나, 서비스 로직에 전달
        MissionResponseDto response = missionService.createMission(requestDto, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * [PATCH] 미션 수정
     * - 지정한 ID의 미션 정보를 수정한다.
     *
     * @param id         수정 대상 미션 ID
     * @param requestDto 수정 요청 DTO
     * @return 수정된 미션 정보
     */
    @Operation(summary = "미션 수정", description = "기존 미션 정보를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<MissionResponseDto> updateMission(
            @PathVariable("id") Long id,
            @RequestBody MissionRequestDto requestDto
    ) {
        return ResponseEntity.ok(missionService.updateMission(id, requestDto));
    }

    /**
     * [DELETE] 미션 삭제
     * - 지정한 ID의 미션을 삭제한다.
     *
     * @param id 삭제할 미션 ID
     * @return HTTP 204 No Content
     */
    @Operation(summary = "미션 삭제", description = "미션을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable("id") Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * [GET] 미션 단건 조회
     * - 지정한 ID의 미션 상세 정보를 조회한다.
     *
     * @param id 조회할 미션 ID
     * @return 미션 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMission(@PathVariable("id") Long id) {
        try {
            MissionResponseDto mission = missionService.getMission(id);
            return ResponseEntity.ok(mission);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("존재하지 않습니다")) {
                return ResponseEntity.status(404).body(e.getMessage());
            }
            return ResponseEntity.status(500).body("서버 오류 발생");
        }
    }

    /**
     * [GET] 미션 전체 목록 조회
     * - 등록된 전체 미션 목록을 조회한다.
     *
     * @return 미션 리스트
     */
    @Operation(summary = "미션 전체 목록 조회", description = "등록된 모든 미션 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<MissionResponseDto>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }
}

