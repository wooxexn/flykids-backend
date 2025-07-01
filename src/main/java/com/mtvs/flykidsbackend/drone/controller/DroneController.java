package com.mtvs.flykidsbackend.drone.controller;

import com.mtvs.flykidsbackend.drone.dto.DronePositionRequestDto;
import com.mtvs.flykidsbackend.drone.dto.DroneResponse;
import com.mtvs.flykidsbackend.drone.service.DronePositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
     * @return 상태 코드 + JSON 메시지
     */
    @Operation(
            summary = "드론 위치 기록 및 경로 이탈 판단",
            description = "드론의 현재 위치를 기록하고 기준 경로와 비교하여 이탈 여부를 판단합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "요청 성공 (정상 저장 또는 경고 메시지 포함)",
                            content = @Content(schema = @Schema(implementation = DroneResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류"
                    )
            }
    )
    @PostMapping("/position-log")
    public ResponseEntity<DroneResponse> logDronePosition(@RequestBody DronePositionRequestDto request) {
        DroneResponse response = dronePositionService.savePosition(request);

        // 상태 코드 구분 (예: OK or 경고는 200, 예외는 400 이상)
        if (response.getStatus().equals("OK")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(response); // 경고도 200으로 보냄
        }
    }
}