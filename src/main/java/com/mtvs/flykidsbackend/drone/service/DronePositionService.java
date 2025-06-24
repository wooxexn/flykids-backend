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
 * 예외 발생 시 적절한 메시지로 처리되도록 수정함.
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
     * @throws IllegalArgumentException 미션 ID 또는 드론 ID가 없거나 유효하지 않을 경우
     * @throws RuntimeException DB 저장 중 오류 발생 시
     */
    public String savePosition(DronePositionRequestDto requestDto) {
        // 입력값 검증
        if (requestDto == null
                || requestDto.getMissionId() == null || requestDto.getMissionId() <= 0
                || requestDto.getDroneId() == null || requestDto.getDroneId().isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 드론 위치 정보입니다.");
        }

        try {
            // 드론 위치 로그 엔티티 생성 및 저장
            DronePositionLog log = DronePositionLog.builder()
                    .droneId(requestDto.getDroneId())
                    .missionId(requestDto.getMissionId())
                    .x(requestDto.getX())
                    .y(requestDto.getY())
                    .z(requestDto.getZ())
                    .rotationY(requestDto.getRotationY())
                    .build();

            dronePositionLogRepository.save(log);

            // 미션 ID 기반 기준 경로 조회
            List<RoutePoint> routePoints = routePointService.getRouteByMissionId(requestDto.getMissionId());

            // 기준 경로 존재 여부 체크
            if (routePoints == null || routePoints.isEmpty()) {
                throw new IllegalArgumentException("해당 미션의 기준 경로가 존재하지 않습니다.");
            }

            // 기준 경로와 비교하여 경로 이탈 여부 판단
            boolean isOut = isOutOfRoute(log, routePoints);

            if (isOut) {
                // 경로 이탈 로그 생성 및 저장
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

        } catch (Exception ex) {
            throw new RuntimeException("드론 위치 저장 중 오류가 발생했습니다: " + ex.getMessage(), ex);
        }
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