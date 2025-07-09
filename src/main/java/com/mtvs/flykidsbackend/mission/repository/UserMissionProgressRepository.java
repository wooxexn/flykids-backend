package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.model.UserMissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionProgressRepository extends JpaRepository<UserMissionProgress, Long> {

    // 유저 + 미션 단일 진행 기록 조회 (유일해야 하므로 Optional)
    Optional<UserMissionProgress> findByUserAndMission(User user, Mission mission);

    // 유저의 전체 미션 진행 기록
    List<UserMissionProgress> findByUser(User user);

    // 유저 + 상태 기반 미션 진행 기록 (예: COMPLETED만 조회)
    List<UserMissionProgress> findByUserAndStatus(User user, UserMissionStatus status);

     // 특정 유저와 미션 및 상태에 따른 미션 진행 기록 리스트 조회
     List<UserMissionProgress> findByUserAndMissionAndStatus(User user, Mission mission, UserMissionStatus status);

}
