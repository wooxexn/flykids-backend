package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;

import java.util.List;
import java.util.Optional;

/**
 * 유저 미션 진행 상태 관리 서비스 인터페이스
 * - 유저별 미션 및 미션 아이템 단계별 진행 정보를 관리한다.
 */
public interface UserMissionProgressService {

    /**
     * 특정 유저, 미션, 미션 아이템의 진행 정보 조회
     * @param user 대상 유저
     * @param mission 대상 미션 퀘스트
     * @param missionItem 미션의 특정 단계 아이템
     * @return 진행 정보가 Optional로 반환됨 (없으면 Optional.empty)
     */
    Optional<UserMissionProgress> getProgress(User user, Mission mission, MissionItem missionItem);

    /**
     * 특정 유저와 미션에 대한 모든 단계별 진행 정보 리스트 조회
     * @param user 대상 유저
     * @param mission 대상 미션 퀘스트
     * @return 진행 정보 리스트
     */
    List<UserMissionProgress> getProgressList(User user, Mission mission);

    /**
     * 유저 미션 진행 정보를 저장하거나 수정
     * @param progress 저장/수정할 진행 정보 엔티티
     * @return 저장된 진행 정보 엔티티
     */
    UserMissionProgress saveOrUpdateProgress(UserMissionProgress progress);

    /**
     * 특정 상태(예: 완료, 진행중 등)에 해당하는 미션 진행 정보 리스트 조회
     * @param user 대상 유저
     * @param mission 대상 미션 퀘스트
     * @param status 상태 문자열 (예: "COMPLETED")
     * @return 해당 상태에 속하는 진행 정보 리스트
     */
    List<UserMissionProgress> getProgressByStatus(User user, Mission mission, String status);

    /**
     * 특정 미션 아이템 단계의 진행 상태를 변경하거나 새로 저장
     * @param user 대상 유저
     * @param mission 대상 미션 퀘스트
     * @param missionItem 단계별 미션 아이템
     * @param newStatus 변경할 상태 값
     */
    void updateStatus(User user, Mission mission, MissionItem missionItem, String newStatus);
}

