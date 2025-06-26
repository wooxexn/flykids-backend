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
@Tag(name = "기준 경로", description = "드론이 따라야 할 기준 경로 좌표 등록 및 조회 API")
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
    @Operation(summary = "기준 경로 좌표 등록", description = "여러 기준 경로 좌표를 한 번에 등록합니다.")
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
    @Operation(summary = "기준 경로 조회", description = "미션 ID에 해당하는 기준 경로 좌표 리스트를 조회합니다.")
    @GetMapping("/points")
    public ResponseEntity<List<RoutePoint>> getRoutePoints(@RequestParam Long missionId) {
        List<RoutePoint> points = routePointService.getRouteByMissionId(missionId);
        return ResponseEntity.ok(points);
    }
}
