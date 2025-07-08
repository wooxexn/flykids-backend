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
@Tag(
        name = "Drone",
        description = "드론의 위치 기록과 기준 경로와의 비교를 통한 경로 이탈 판단을 처리하는 API입니다. " +
                "드론이 실시간 위치 데이터를 서버에 전송할 때 호출되며, 안전한 비행 상태 모니터링을 지원합니다."
)
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
            description = "드론이 실시간으로 위치 데이터를 서버에 전송할 때 호출합니다. " +
                    "서버는 이 위치를 저장하고, 기준 경로와 비교하여 이탈 여부를 판단해 경고 메시지를 생성합니다. " +
                    "이를 통해 사용자는 비행 상태를 모니터링하고 안전한 조작이 가능하도록 지원합니다."
    )
    @PostMapping("/position-log")
    public ResponseEntity<DroneResponse> logDronePosition(@RequestBody DronePositionRequestDto request) {
        DroneResponse response = dronePositionService.savePosition(request);
        return ResponseEntity.ok(response);
    }
}