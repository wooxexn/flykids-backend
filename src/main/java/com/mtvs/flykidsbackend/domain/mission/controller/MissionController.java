package com.mtvs.flykidsbackend.domain.mission.controller;

import com.mtvs.flykidsbackend.common.AudioFilePath;
import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.domain.mission.dto.IntroAudioResponseDto;
import com.mtvs.flykidsbackend.domain.mission.dto.IntroMessageResponseDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.domain.mission.entity.Mission;
import com.mtvs.flykidsbackend.domain.mission.service.MissionService;
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
@Tag(
        name = "Mission",
        description = "미션 등록, 수정, 삭제, 조회 등 미션 관리에 필요한 모든 API를 제공합니다."
)
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
    @Operation(
            summary = "미션 등록",
            description = "사용자가 새로운 미션을 등록할 때 호출합니다. " +
                    "미션 제목, 제한 시간, 유형 정보를 받아 미션 데이터를 생성 및 저장합니다. " +
                    "이 API를 통해 관리자는 새로운 미션을 추가할 수 있습니다."
    )
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
    @Operation(
            summary = "미션 수정",
            description = "관리자가 기존에 등록된 미션 정보를 수정할 때 호출합니다. " +
                    "미션 제목, 제한 시간, 미션 아이템 등을 변경하여 최신 상태로 유지하기 위해 사용됩니다."
    )
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
    @Operation(
            summary = "미션 삭제",
            description = "관리자가 더 이상 필요하지 않은 미션을 삭제할 때 호출합니다. " +
                    "미션을 시스템에서 완전히 제거하여 관리 효율성을 높이기 위해 사용됩니다."
    )
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
    @Operation(
            summary = "미션 단건 조회",
            description = "관리자 또는 서비스가 특정 미션의 상세 정보를 조회할 때 호출합니다. " +
                    "미션의 제목, 제한 시간, 아이템 정보 등을 확인하여 화면에 표시하거나 내부 처리에 활용하기 위해 사용됩니다."
    )
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
    @Operation(
            summary = "미션 전체 목록 조회",
            description = "관리자나 서비스가 등록된 모든 미션의 목록을 한 번에 조회할 때 호출합니다. " +
                    "새로운 미션 추가, 수정, 삭제 시 전체 목록을 최신 상태로 유지하거나 사용자에게 보여주기 위해 사용됩니다."
    )
    @GetMapping
    public ResponseEntity<List<MissionResponseDto>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    /**
     * 미션 시작 안내 멘트 조회 API
     *
     * - 특정 미션 ID에 해당하는 시작 멘트를 반환한다
     * - 클라이언트(예: 유니티)가 미션 시작 전, 드론 NPC의 음성 또는 자막으로 활용 가능
     *
     * @param id 조회할 미션 ID (Path Variable)
     * @return introMessage 텍스트 (Map 형태로 반환)
     * @throws IllegalArgumentException 존재하지 않는 미션 ID인 경우
     */
    @Operation(
            summary = "미션 시작 멘트 조회",
            description = "미션 시작 전 사용자에게 안내할 텍스트 멘트를 반환합니다. " +
                    "유니티 클라이언트에서 자막 또는 음성 처리용으로 활용 가능합니다."
    )
    @GetMapping("/{id}/intro")
    public ResponseEntity<IntroMessageResponseDto> getIntroMessage(@PathVariable("id") Long id) {
        Mission mission = missionService.getMissionEntity(id);

        IntroMessageResponseDto response = IntroMessageResponseDto.builder()
                .missionId(mission.getId())
                .introMessage(mission.getIntroMessage())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 미션 시작 안내 멘트의 TTS 음성 URL 조회 API
     *
     * <기능 요약>
     * - 클라이언트가 특정 미션을 시작할 때 필요한 TTS 음성(mp3) URL을 반환한다.
     * - AI 서버에 요청하지 않고, S3에 미리 업로드된 mp3 파일 URL을 미션 ID에 따라 매핑하여 제공한다.
     *
     * <사용 예시>
     * - 미션 1 시작 시: GET /api/missions/1/intro/audio → mission1_intro.mp3 URL 반환
     *
     * @param id 시작 안내 음성을 가져올 미션 ID
     * @return 해당 미션에 맞는 시작 음성 mp3 파일의 S3 URL을 포함한 응답 DTO
     */
    @Operation(
            summary = "미션 시작 안내 멘트의 TTS 음성 URL 조회",
            description = "S3에 저장된 미리 생성된 시작 안내 mp3 URL을 반환합니다."
    )
    @GetMapping("/{id}/intro/audio")
    public ResponseEntity<IntroAudioResponseDto> getIntroAudio(@PathVariable("id") Long id) {
        String audioUrl;

        // 미션 ID에 따라 미리 지정된 mp3 파일명 매핑
        audioUrl = switch (id.intValue()) {
            case 1 -> AudioFilePath.MISSION1_INTRO;
            case 2 -> AudioFilePath.MISSION2_INTRO;
            case 3 -> AudioFilePath.MISSION3_INTRO;
            default -> throw new IllegalArgumentException("지원하지 않는 미션 ID입니다.");
        };

        return ResponseEntity.ok(IntroAudioResponseDto.builder()
                .missionId(id)
                .audioUrl(audioUrl)
                .build());
    }
}

