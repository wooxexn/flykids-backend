package com.mtvs.flykidsbackend.drone.controller;

import com.mtvs.flykidsbackend.drone.dto.DronePositionRequestDto;
import com.mtvs.flykidsbackend.drone.service.DronePositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *  드론 위치 기록 컨트롤러
 *
 * 유니티에서 전달한 드론의 실시간 좌표 데이터를 수신하여 서비스로 전달한다.
 * 드론 위치를 저장하고, 기준 경로에서의 이탈 여부를 판단한다.
 */
@Tag(name = "드론", description = "드론 위치 기록 및 경로 이탈 판단 API")
@RestController
@RequestMapping("/api/drone")
@RequiredArgsConstructor
public class DroneController {

    private final DronePositionService dronePositionService;

    /**
     * 드론 위치 데이터 저장 및 경로 이탈 여부 판단
     *
     * POST /api/drone/position-log
     *
     * @param request 드론 좌표 요청 DTO
     * @return 성공 또는 경고 메시지
     */
    @Operation(summary = "드론 위치 기록 및 경로 이탈 판단", description = "드론의 현재 위치를 기록하고 기준 경로와 비교하여 이탈 여부를 판단합니다.")
    @PostMapping("/position-log")
    public ResponseEntity<?> logDronePosition(@RequestBody DronePositionRequestDto request) {
        try {
            String resultMessage = dronePositionService.savePosition(request);
            return ResponseEntity.ok(resultMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
