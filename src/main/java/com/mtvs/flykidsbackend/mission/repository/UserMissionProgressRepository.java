package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionProgressRepository extends JpaRepository<UserMissionProgress, Long> {

    // 특정 유저 + 미션 + 미션 아이템에 대한 진행 기록 조회 (Optional로 단일 결과)
    Optional<UserMissionProgress> findByUserAndMissionAndMissionItem(User user, Mission mission, MissionItem missionItem);

    // 특정 유저 + 미션에 대한 모든 단계 진행 기록 조회
    List<UserMissionProgress> findByUserAndMission(User user, Mission mission);

    // 특정 유저 + 미션 + 상태별 진행 기록 조회 (예: 완료된 단계만 조회)
    List<UserMissionProgress> findByUserAndMissionAndStatus(User user, Mission mission, String status);
}
