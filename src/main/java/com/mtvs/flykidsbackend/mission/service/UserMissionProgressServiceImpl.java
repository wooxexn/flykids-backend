package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.repository.UserMissionProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 유저 미션 진행 상태 관리 서비스 구현체
 * - 유저와 미션 단위로 진행 상태를 관리한다.
 */
@Service
@RequiredArgsConstructor
public class UserMissionProgressServiceImpl implements UserMissionProgressService {

    private final UserMissionProgressRepository progressRepository;

    /**
     * 특정 유저와 미션의 진행 정보를 조회한다.
     */
    @Override
    public Optional<UserMissionProgress> getProgress(User user, Mission mission) {
        return progressRepository.findByUserAndMission(user, mission);
    }

    /**
     * 특정 유저의 모든 미션 진행 정보를 조회한다.
     */
    @Override
    public List<UserMissionProgress> getAllProgress(User user) {
        return progressRepository.findByUser(user);
    }

    /**
     * 유저 미션 진행 정보를 저장하거나 수정한다.
     */
    @Override
    public UserMissionProgress saveOrUpdateProgress(UserMissionProgress progress) {
        return progressRepository.save(progress);
    }

    /**
     * 특정 상태의 미션 진행 정보 리스트를 조회한다.
     */
    @Override
    public List<UserMissionProgress> getProgressByStatus(User user, String status) {
        return progressRepository.findByUserAndStatus(user, status);
    }

    /**
     * 특정 미션의 진행 상태를 변경하거나 새로 생성하여 저장한다.
     */
    @Override
    public void updateStatus(User user, Mission mission, String newStatus) {
        Optional<UserMissionProgress> opt = getProgress(user, mission);
        UserMissionProgress progress = opt.orElse(
                UserMissionProgress.builder()
                        .user(user)
                        .mission(mission)
                        .status(newStatus)
                        .build()
        );
        progress.setStatus(newStatus);
        saveOrUpdateProgress(progress);
    }

    /**
     * 유저와 미션에 대한 진행 정보가 없으면 새로 생성한다.
     *
     * @param user 대상 유저
     * @param mission 대상 미션
     * @param status 초기 상태 (예: "READY")
     */
    @Override
    public void createIfNotExist(User user, Mission mission, String status) {
        // 해당 유저와 미션에 대한 진행 상태 리스트 조회;
        Optional<UserMissionProgress> existing = getProgress(user, mission);

        // 진행 정보가 없으면 새 객체를 생성하여 저장
        if (existing.isEmpty()) {
            UserMissionProgress newProgress = UserMissionProgress.builder()
                    .user(user)
                    .mission(mission)
                    .status(status)
                    .build();
            progressRepository.save(newProgress);
        }
    }

}
