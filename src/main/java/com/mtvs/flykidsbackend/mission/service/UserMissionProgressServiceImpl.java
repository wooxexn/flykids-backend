package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.repository.UserMissionProgressRepository;
import com.mtvs.flykidsbackend.user.model.UserMissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMissionProgressServiceImpl implements UserMissionProgressService {

    private final UserMissionProgressRepository progressRepository;

    /** 유저-미션 단일 조회 */
    @Override
    public Optional<UserMissionProgress> getProgress(User user, Mission mission) {
        return progressRepository.findByUserAndMission(user, mission);
    }

    /** 유저의 전체 진행 정보 조회 */
    @Override
    public List<UserMissionProgress> getAllProgress(User user) {
        return progressRepository.findByUser(user);
    }

    /** 저장 또는 수정 */
    @Override
    public UserMissionProgress saveOrUpdateProgress(UserMissionProgress progress) {
        return progressRepository.save(progress);
    }

    /** 특정 상태(READY, SUCCESS 등) 기반 조회 */
    @Override
    public List<UserMissionProgress> getProgressByStatus(User user, UserMissionStatus status) {
        return progressRepository.findByUserAndStatus(user, status);
    }

    /** 상태 변경 또는 생성 */
    @Override
    public void updateStatus(User user, Mission mission, UserMissionStatus newStatus) {
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

    /** 존재하지 않으면 초기 상태로 생성 */
    @Override
    public void createIfNotExist(User user, Mission mission, UserMissionStatus status) {
        if (getProgress(user, mission).isEmpty()) {
            UserMissionProgress newProgress = UserMissionProgress.builder()
                    .user(user)
                    .mission(mission)
                    .status(status)
                    .build();
            progressRepository.save(newProgress);
        }
    }
}
