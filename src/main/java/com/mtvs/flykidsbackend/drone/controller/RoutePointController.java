package com.mtvs.flykidsbackend.drone.controller;

import com.mtvs.flykidsbackend.drone.dto.RoutePointRequestDto;
import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.drone.service.RoutePointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 기준 경로 API 컨트롤러
 * 여러 좌표를 한 번에 등록하거나 조회할 수 있다.
 */
@Tag(
        name = "Route",
        description = "드론이 따라야 할 기준 경로 좌표를 등록하고 조회하는 API입니다. 여러 좌표를 한 번에 처리할 수 있습니다."
)
@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RoutePointController {

    private final RoutePointService routePointService;

    /**
     * 기준 경로 좌표 여러 개 저장
     * POST /api/route/points
     *
     * @param pointList 기준 경로 좌표 리스트
     * @return 성공 메시지
     */
    @Operation(
            summary = "기준 경로 좌표 등록",
            description = "사용자가 드론 비행의 기준이 될 여러 경로 좌표를 한 번에 서버에 등록합니다. " +
                    "비행 경로 설정이나 미션 준비 시 호출되며, " +
                    "등록된 경로는 드론 위치 이탈 판단과 미션 수행 평가에 사용됩니다."
    )
    @PostMapping("/points")
    public ResponseEntity<String> saveRoutePoints(@RequestBody List<RoutePointRequestDto> pointList) {
        try {
            routePointService.saveRoutePoints(pointList);
            return ResponseEntity.ok("기준 경로가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 기준 경로 조회 API
     *
     * GET /api/route/points?missionId=1
     * 미션 ID에 해당하는 기준 경로 포인트 리스트를 조회한다.
     *
     * @param missionId 조회할 미션 ID
     * @return 기준 경로 좌표 리스트
     */
    @Operation(
            summary = "기준 경로 조회",
            description = "특정 미션에 설정된 기준 경로 좌표 리스트를 조회합니다. " +
                    "미션 수행 전 드론 비행 경로 확인이나 경로 검증을 위해 호출됩니다. " +
                    "이 데이터를 통해 클라이언트는 드론 경로를 시각화하거나 이탈 판단에 활용할 수 있습니다."
    )
    @GetMapping("/points")
    public ResponseEntity<List<RoutePoint>> getRoutePoints(@RequestParam("missionId") Long missionId) {
        List<RoutePoint> points = routePointService.getRouteByMissionId(missionId);
        return ResponseEntity.ok(points);
    }
}
