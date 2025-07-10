package com.mtvs.flykidsbackend.drone.service;

import com.mtvs.flykidsbackend.common.AudioFilePath;
import com.mtvs.flykidsbackend.drone.dto.DronePositionRequestDto;
import com.mtvs.flykidsbackend.drone.dto.DroneResponse;
import com.mtvs.flykidsbackend.drone.entity.DronePositionLog;
import com.mtvs.flykidsbackend.drone.entity.RouteDeviationLog;
import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.drone.repository.DronePositionLogRepository;
import com.mtvs.flykidsbackend.drone.repository.RouteDeviationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 드론 위치 처리 서비스
 * <p>
 * 유니티 클라이언트에서 수신한 드론 좌표 데이터를 DB에 저장하고,
 * 기준 경로와 비교하여 경로 이탈, 고도 이탈, 충돌 여부를 판단한다.
 */
@Service
@RequiredArgsConstructor
public class DronePositionService {

    private final DronePositionLogRepository dronePositionLogRepository;
    private final RoutePointService routePointService;
    private final RouteDeviationLogRepository routeDeviationLogRepository;

    private static final double ALLOWED_DISTANCE = 2.5; // 경로 이탈 허용 수평 거리(m)
    private static final double MIN_ALTITUDE = 0.5; // 최소 고도(m)
    private static final double MAX_ALTITUDE = 3.0; // 최대 고도(m)

    /**
     * 드론 위치 데이터를 저장하고 경로 이탈, 고도 이탈, 충돌 여부를 판단한다.
     *
     * @param requestDto 드론 위치 요청 DTO
     * @return DroneResponse(status, message, audioUrl)
     */
    public DroneResponse savePosition(DronePositionRequestDto requestDto) {

        try {
            // 요청 값 유효성 검사
            if (requestDto == null
                    || requestDto.getMissionId() == null || requestDto.getMissionId() <= 0
                    || requestDto.getDroneId() == null || requestDto.getDroneId().isBlank()) {
                throw new IllegalArgumentException("유효하지 않은 드론 위치 정보입니다.");
            }

            // 위치 로그 생성 & 저장
            DronePositionLog log = DronePositionLog.builder()
                    .droneId(requestDto.getDroneId())
                    .missionId(requestDto.getMissionId())
                    .x(requestDto.getX())
                    .y(requestDto.getY())
                    .z(requestDto.getZ())
                    .rotationY(requestDto.getRotationY())
                    .build();

            dronePositionLogRepository.save(log);

            // 기준 경로 조회
            List<RoutePoint> routePoints =
                    routePointService.getRouteByMissionId(requestDto.getMissionId());

            if (routePoints == null || routePoints.isEmpty()) {
                throw new IllegalArgumentException("해당 미션의 기준 경로가 존재하지 않습니다.");
            }

            // 충돌 추정 판단
            Optional<DronePositionLog> optPrev =
                    dronePositionLogRepository
                            .findTopByDroneIdAndLoggedAtBeforeOrderByLoggedAtDesc(
                                    requestDto.getDroneId(), log.getLoggedAt());

            if (optPrev.isPresent()) {
                DronePositionLog prev = optPrev.get();

                double deltaY        = Math.abs(log.getY() - prev.getY());
                double deltaRot      = Math.abs(log.getRotationY() - prev.getRotationY());
                double deltaDistance = calculateDistance(log, prev);

                if (deltaY > 0.7 || deltaDistance < 0.1 || deltaRot > 45.0) {
                    saveDeviationLog(log);
                    return new DroneResponse(
                            "COLLISION",
                            "경고: 충돌이 감지되었습니다.",
                            AudioFilePath.FEEDBACK_COLLISION
                    );
                }
            }

            // 고도 이탈 체크
            double y = log.getY();
            if (y < MIN_ALTITUDE) {
                saveDeviationLog(log);
                return new DroneResponse(
                        "ALTITUDE_LOW",
                        "경고: 고도가 너무 낮습니다.",
                        AudioFilePath.FEEDBACK_ALTITUDE_LOW
                );
            } else if (y > MAX_ALTITUDE) {
                saveDeviationLog(log);
                return new DroneResponse(
                        "ALTITUDE_HIGH",
                        "경고: 고도가 너무 높습니다.",
                        AudioFilePath.FEEDBACK_ALTITUDE_HIGH
                );
            }

            // 경로 이탈 체크
            if (isOutOfRoute(log, routePoints)) {
                saveDeviationLog(log);
                return new DroneResponse(
                        "OUT_OF_BOUNDS",
                        "경고: 드론이 기준 경로를 이탈했습니다.",
                        AudioFilePath.FEEDBACK_DEVIATION
                );
            }

            // 정상 처리
            return new DroneResponse(
                    "OK",
                    "드론 위치가 정상적으로 저장되었습니다.",
                    null
            );

        } catch (Exception ex) {
            return new DroneResponse(
                    "ERROR",
                    "드론 위치 저장 중 오류 발생: " + ex.getMessage(),
                    null
            );
        }
    }

    /**
     * 이탈/충돌 로그 저장
     */
    private void saveDeviationLog(DronePositionLog log) {
        RouteDeviationLog deviationLog = RouteDeviationLog.builder()
                .missionId(log.getMissionId())
                .droneId(log.getDroneId())
                .x(log.getX())
                .y(log.getY())
                .z(log.getZ())
                .rotationY(log.getRotationY())
                .timestamp(LocalDateTime.now())
                .build();

        routeDeviationLogRepository.save(deviationLog);
    }

    /**
     * 기준 경로의 한 점과 드론 위치 간 거리 계산
     */
    private double calculateDistance(DronePositionLog log, RoutePoint route) {
        double dx = log.getX() - route.getX();
        double dy = log.getY() - route.getY();
        double dz = log.getZ() - route.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 두 위치 로그 간 거리 계산
     */
    private double calculateDistance(DronePositionLog a, DronePositionLog b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 기준 경로로부터 일정 거리 이상 이탈했는지 판단
     */
    private boolean isOutOfRoute(DronePositionLog log, List<RoutePoint> routePoints) {
        double minDistance = Double.MAX_VALUE;
        for (RoutePoint route : routePoints) {
            double distance = calculateDistance(log, route);
            minDistance = Math.min(minDistance, distance);
        }
        return minDistance > ALLOWED_DISTANCE;
    }
}
