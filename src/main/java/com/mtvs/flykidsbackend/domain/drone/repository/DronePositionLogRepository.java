package com.mtvs.flykidsbackend.domain.drone.repository;

import com.mtvs.flykidsbackend.domain.drone.entity.DronePositionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 드론 위치 로그 Repository
 *
 * DronePositionLog 엔티티에 대한 DB 접근을 담당하는 JPA Repository 인터페이스
 */
@Repository
public interface DronePositionLogRepository extends JpaRepository<DronePositionLog, Long> {

    /**
     * 특정 미션 ID에 해당하는 드론 위치 로그 전체 조회
     *
     * @param missionId 미션 식별자
     * @return 해당 미션의 위치 로그 목록
     */
    List<DronePositionLog> findByMissionId(Long missionId);

    /**
     * 특정 드론 ID에 해당하는 모든 위치 로그 조회
     *
     * @param droneId 드론 식별자
     * @return 해당 드론의 위치 로그 목록
     */
    List<DronePositionLog> findByDroneId(String droneId);

    /**
     * 특정 드론 ID에 대한 가장 최근 위치 로그 1건 조회
     * - 충돌 추정 판단을 위해 사용
     * - loggedAt 기준 내림차순 정렬 후 첫 번째 데이터 반환
     *
     * @param droneId 드론 식별자
     * @return 가장 최신 위치 로그 (Optional)
     */
    Optional<DronePositionLog> findTopByDroneIdOrderByLoggedAtDesc(String droneId);

    /**
     * 특정 드론 ID에 대해 지정된 시각 이전의 가장 최근 위치 로그 1건 조회
     *
     * - 충돌 추정 시, 현재 위치 로그 이전의 마지막 위치 로그를 조회하기 위해 사용
     * - loggedAt 값이 before 시각보다 작은 것들 중, 가장 최신 로그 1건을 반환
     *
     * @param droneId 드론 식별자
     * @param before 현재 위치 로그의 시간 (이 시간보다 이전 로그만 조회)
     * @return Optional 형태의 가장 최근 위치 로그 (없을 경우 빈 Optional 반환)
     */
    Optional<DronePositionLog> findTopByDroneIdAndLoggedAtBeforeOrderByLoggedAtDesc(String droneId, LocalDateTime before);

}
