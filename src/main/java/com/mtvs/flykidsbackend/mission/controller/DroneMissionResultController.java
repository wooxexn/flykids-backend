package com.mtvs.flykidsbackend.mission.controller;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.service.DroneMissionResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 미션 결과 관련 API 컨트롤러
 * - 미션 완료 후 점수 저장 API 제공
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "DroneMissionResult", description = "드론 미션 결과 API")
public class DroneMissionResultController {

    private final DroneMissionResultService resultService;

    /**
     * 미션 수행 결과 저장 API
     *
     * @param missionId   수행한 미션 ID
     * @param requestDto  클라이언트로부터 전달받은 결과 정보
     * @param request     HTTP 요청 객체 (JWT에서 userId 추출용)
     * @return 저장된 결과 또는 실패 메시지
     */
    @PostMapping("/{missionId}/complete")
    @Operation(summary = "미션 수행 결과 저장", description = "점수, 비행 시간, 이탈 횟수 등 미션 수행 결과를 저장한다.")
    public ResponseEntity<?> completeMission(
            @PathVariable Long missionId,
            @RequestBody DroneMissionResultRequestDto requestDto,
            HttpServletRequest request
    ) {
        try {
            // JWT 토큰에서 인증된 사용자 ID 추출 (Filter 등에서 세팅된 값)
            Long userId = (Long) request.getAttribute("userId");

            DroneMissionResult result = resultService.saveResult(userId, missionId, requestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("미션 결과 저장 중 오류가 발생했습니다.");
        }
    }
}
