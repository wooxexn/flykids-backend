package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.DroneMissionItemResult;
import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroneMissionItemResultRepository extends JpaRepository<DroneMissionItemResult, Long> {

    /** 유저 + 미션 기준으로 전체 단계 결과 조회 */
    List<DroneMissionItemResult> findByUserIdAndMissionId(Long userId, Long missionId);

    /** 유저 + 미션 + 실패한 단계만 조회 (재도전용) */
    List<DroneMissionItemResult> findByUserIdAndMissionIdAndStatus(Long userId, Long missionId, MissionResultStatus status);

    /** 유저 + 미션 + 미션아이템 기준으로 단건 조회 */
    Optional<DroneMissionItemResult> findByUserIdAndMissionIdAndMissionItemId(Long userId, Long missionId, Long missionItemId);
}
