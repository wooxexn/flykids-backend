package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.mission.service.MissionService;
import com.mtvs.flykidsbackend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

/**
 * 미션 결과 관련 API 컨트롤러
 * - 미션 완료 후 점수 계산 및 결과 저장, TTS 응답 포함
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "DroneMissionResult", description = "드론 미션 결과 API")
public class DroneMissionResultController {

    private final MissionService missionService;

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
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            MissionCompleteResponseDto response =
                    missionService.completeMission(userId, missionId, requestDto);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}