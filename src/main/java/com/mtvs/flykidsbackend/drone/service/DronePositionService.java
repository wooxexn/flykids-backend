package com.mtvs.flykidsbackend.drone.service;

import com.mtvs.flykidsbackend.drone.dto.DronePositionRequestDto;
import com.mtvs.flykidsbackend.drone.entity.DronePositionLog;
import com.mtvs.flykidsbackend.drone.entity.RouteDeviationLog;
import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.drone.repository.DronePositionLogRepository;
import com.mtvs.flykidsbackend.drone.repository.RouteDeviationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 드론 위치 처리 서비스
 *
 * 유니티 클라이언트에서 수신한 드론 좌표 데이터를 DB에 저장하고,
 * 기준 경로와 비교하여 경로 이탈 여부를 판단한다.
 */
@Service
@RequiredArgsConstructor
public class DronePositionService {

    private final DronePositionLogRepository dronePositionLogRepository;
    private final RoutePointService routePointService;
    private final RouteDeviationLogRepository routeDeviationLogRepository;

    /**
     * 드론 위치 데이터를 DB에 저장하고,
     * 기준 경로와 비교하여 경로 이탈 여부를 판단한다.
     * 이탈 시, 경로 이탈 로그를 저장하고 경고 메시지를 반환한다.
     *
     * @param requestDto 클라이언트에서 전송한 드론 위치 정보
     * @return 이탈 여부 및 메시지를 포함한 응답 문자열
     */
    public String savePosition(DronePositionRequestDto requestDto) {
        DronePositionLog log = DronePositionLog.builder()
                .droneId(requestDto.getDroneId())
                .missionId(requestDto.getMissionId())
                .x(requestDto.getX())
                .y(requestDto.getY())
                .z(requestDto.getZ())
                .rotationY(requestDto.getRotationY())
                .build();

        dronePositionLogRepository.save(log);

        List<RoutePoint> routePoints = routePointService.getRouteByMissionId(requestDto.getMissionId());

        boolean isOut = isOutOfRoute(log, routePoints);

        if (isOut) {
            RouteDeviationLog deviationLog = RouteDeviationLog.builder()
                    .missionId(requestDto.getMissionId())
                    .droneId(requestDto.getDroneId())
                    .x(requestDto.getX())
                    .y(requestDto.getY())
                    .z(requestDto.getZ())
                    .rotationY(requestDto.getRotationY())
                    .timestamp(LocalDateTime.now())
                    .build();

            routeDeviationLogRepository.save(deviationLog);

            return "경고: 드론이 기준 경로를 이탈했습니다.";
        }

        return "드론 위치가 정상적으로 저장되었습니다.";
    }

    /**
     * 두 좌표 간의 유클리드 거리(Euclidean distance)를 계산한다.
     */
    private double calculateDistance(DronePositionLog log, RoutePoint route) {
        double dx = log.getX() - route.getX();
        double dy = log.getY() - route.getY();
        double dz = log.getZ() - route.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 드론이 기준 경로로부터 일정 거리 이상 이탈했는지 확인한다.
     */
    private boolean isOutOfRoute(DronePositionLog log, List<RoutePoint> routePoints) {
        double minDistance = Double.MAX_VALUE;

        for (RoutePoint route : routePoints) {
            double distance = calculateDistance(log, route);
            minDistance = Math.min(minDistance, distance);
        }

        double threshold = 2.0; // 허용 오차 (예: 2m)
        return minDistance > threshold;
    }
}
