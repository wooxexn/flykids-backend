package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.repository.UserMissionProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 유저 미션 진행 상태 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class UserMissionProgressServiceImpl implements UserMissionProgressService {

    private final UserMissionProgressRepository progressRepository;

    /**
     * 특정 유저, 미션, 미션 아이템 단계별 진행 정보 조회
     */
    @Override
    public Optional<UserMissionProgress> getProgress(User user, Mission mission, MissionItem missionItem) {
        return progressRepository.findByUserAndMissionAndMissionItem(user, mission, missionItem);
    }

    /**
     * 특정 유저와 미션에 대한 모든 단계별 진행 정보 리스트 조회
     */
    @Override
    public List<UserMissionProgress> getProgressList(User user, Mission mission) {
        return progressRepository.findByUserAndMission(user, mission);
    }

    /**
     * 유저 미션 진행 정보 저장 또는 수정
     */
    @Override
    public UserMissionProgress saveOrUpdateProgress(UserMissionProgress progress) {
        return progressRepository.save(progress);
    }

    /**
     * 특정 상태(예: 완료)에 해당하는 미션 진행 정보 리스트 조회
     */
    @Override
    public List<UserMissionProgress> getProgressByStatus(User user, Mission mission, String status) {
        return progressRepository.findByUserAndMissionAndStatus(user, mission, status);
    }

    /**
     * 특정 단계 미션 진행 상태를 변경하거나 새로 생성하여 저장
     */
    @Override
    public void updateStatus(User user, Mission mission, MissionItem missionItem, String newStatus) {
        Optional<UserMissionProgress> opt = getProgress(user, mission, missionItem);
        UserMissionProgress progress = opt.orElse(
                UserMissionProgress.builder()
                        .user(user)
                        .mission(mission)
                        .missionItem(missionItem)
                        .status(newStatus)
                        .build()
        );
        progress.setStatus(newStatus);
        saveOrUpdateProgress(progress);
    }
}
