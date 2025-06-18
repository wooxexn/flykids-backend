package com.mtvs.flykidsbackend.drone.controller;

import com.mtvs.flykidsbackend.drone.dto.RoutePointRequestDto;
import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.drone.service.RoutePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 기준 경로 API 컨트롤러
 * 여러 좌표를 한 번에 등록하거나 조회할 수 있다.
 */
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
    @PostMapping("/points")
    public ResponseEntity<String> saveRoutePoints(@RequestBody List<RoutePointRequestDto> pointList) {
        routePointService.saveRoutePoints(pointList);
        return ResponseEntity.ok("기준 경로가 저장되었습니다.");
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
    @GetMapping("/points")
    public ResponseEntity<List<RoutePoint>> getRoutePoints(@RequestParam Long missionId) {
        List<RoutePoint> points = routePointService.getRouteByMissionId(missionId);
        return ResponseEntity.ok(points);
    }
}
