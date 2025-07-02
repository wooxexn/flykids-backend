package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionItemRepository extends JpaRepository<MissionItem, Long> {

    // 특정 미션(mission_id)에 속한 MissionItem 리스트 조회
    List<MissionItem> findByMissionId(Long missionId);


    // 특정 미션에 속한 모든 MissionItem 삭제
    void deleteAllByMissionId(Long missionId);
}
