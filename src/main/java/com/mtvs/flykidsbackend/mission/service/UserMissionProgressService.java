package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;

import java.util.List;
import java.util.Optional;

/**
 * 유저 미션 진행 상태 관리 서비스 인터페이스
 * - 유저별로 미션의 성공/실패 여부를 관리한다.
 */
public interface UserMissionProgressService {

    /**
     * 특정 유저와 미션에 대한 진행 정보를 조회한다.
     *
     * @param user    대상 유저
     * @param mission 대상 미션
     * @return Optional<UserMissionProgress> (없으면 Optional.empty 반환)
     */
    Optional<UserMissionProgress> getProgress(User user, Mission mission);

    /**
     * 특정 유저의 모든 미션 진행 정보 리스트를 조회한다.
     *
     * @param user 대상 유저
     * @return 유저가 수행한 모든 미션 진행 정보 리스트
     */
    List<UserMissionProgress> getAllProgress(User user);

    /**
     * 유저 미션 진행 정보를 저장하거나 수정한다.
     *
     * @param progress 저장/수정할 진행 정보 엔티티
     * @return 저장된 진행 정보 엔티티
     */
    UserMissionProgress saveOrUpdateProgress(UserMissionProgress progress);

    /**
     * 특정 상태(예: COMPLETED, FAILED)에 해당하는 미션 진행 정보 리스트를 조회한다.
     *
     * @param user   대상 유저
     * @param status 상태 문자열 (예: "COMPLETED")
     * @return 해당 상태의 미션 리스트
     */
    List<UserMissionProgress> getProgressByStatus(User user, String status);

    /**
     * 특정 미션의 진행 상태를 변경하거나 새로 저장한다.
     *
     * @param user      대상 유저
     * @param mission   대상 미션
     * @param newStatus 변경할 상태 값 (예: "COMPLETED")
     */
    void updateStatus(User user, Mission mission, String newStatus);

    /**
     * 유저가 특정 미션에 대한 진행 정보가 없으면 지정한 상태로 생성
     * @param user 대상 유저
     * @param mission 오픈할 다음 미션
     * @param status 초기 상태 (예: "READY")
     */
    void createIfNotExist(User user, Mission mission, String status);

}
