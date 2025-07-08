package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionItemRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 미션 관리 서비스 구현체
 * - 미션 등록, 수정, 삭제, 조회, 중단 기능을 처리한다
 * - Controller와 Repository 사이의 비즈니스 로직을 담당한다
 */
@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;
    private final MissionItemRepository missionItemRepository;
    private final DroneMissionResultRepository resultRepository;
    private final ScoreCalculator scoreCalculator;

    @Override
    @Transactional
    public MissionResponseDto createMission(MissionRequestDto dto, Long userId) {

        Mission mission = Mission.builder()
                .title(dto.getTitle())
                .timeLimit(dto.getTimeLimit())
                .build();

        dto.getItems().forEach(i -> mission.addItem(
                MissionItem.builder()
                        .title(i.getTitle())
                        .timeLimit(i.getTimeLimit())
                        .type(i.getType())
                        .totalCoinCount(i.getTotalCoinCount())
                        .build()
        ));

        return MissionResponseDto.from(missionRepository.save(mission));
    }

    @Override
    @Transactional
    public MissionResponseDto updateMission(Long id, MissionRequestDto dto) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        mission.setTitle(dto.getTitle());
        mission.setTimeLimit(dto.getTimeLimit());   // 빠져 있던 부분

        mission.getMissionItems().clear();          // orphanRemoval=true → 자동 삭제
        dto.getItems().forEach(i -> mission.addItem(
                MissionItem.builder()
                        .title(i.getTitle())
                        .timeLimit(i.getTimeLimit())
                        .type(i.getType())
                        .totalCoinCount(i.getTotalCoinCount())
                        .build()
        ));

        return MissionResponseDto.from(mission);    // dirty checking
    }

    // 이하 기존 메서드 유지

    @Override
    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 미션이 존재하지 않습니다.");
        }
        missionRepository.deleteById(id);
    }

    @Override
    public MissionResponseDto getMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
        return MissionResponseDto.from(mission);
    }

    @Override
    public List<MissionResponseDto> getAllMissions() {
        return missionRepository.findAll()
                .stream()
                .map(MissionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 미션 완료 처리
     * - 미션 아이템별 결과를 받아 점수를 계산하고 상태(SUCCESS/FAIL) 판단
     * - 결과를 DroneMissionResult 엔티티로 저장
     * - 최종 점수, 소요 시간, 이탈/충돌 횟수, 미션 상태, 안내 메시지 반환
     *
     * @param userId    완료한 유저 ID (JWT 토큰에서 추출)
     * @param missionId 완료한 미션 ID
     * @param dto       미션 아이템별 결과 데이터 DTO
     * @return 미션 완료 결과 응답 DTO
     */
    @Transactional
    @Override
    public MissionCompleteResponseDto completeMission(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        List<DroneMissionResultRequestDto.MissionItemResult> itemResults = dto.getItemResults();
        if (itemResults == null || itemResults.isEmpty()) {
            throw new IllegalArgumentException("미션 아이템 결과가 없습니다.");
        }

        int totalScore = 0;
        boolean allSuccess = true;
        StringBuilder msgBuilder = new StringBuilder();


        // 각 미션 아이템별 결과 처리
        for (var itemResult : itemResults) {
            // 미션에서 해당 타입의 미션 아이템 조회
            MissionItem missionItem = mission.getMissionItems().stream()
                    .filter(mi -> mi.getType() == itemResult.getMissionType())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당 미션 아이템이 존재하지 않습니다."));

            // ScoreCalculator에 점수 계산 위임
            int score = scoreCalculator.calculateScore(
                    itemResult.getMissionType(),
                    itemResult.getTotalTime(),
                    itemResult.getDeviationCount(),
                    itemResult.getCollisionCount(),
                    itemResult.getCollectedCoinCount() != null ? itemResult.getCollectedCoinCount() : 0
            );

            // ScoreCalculator에 성공 여부 판단 위임
            boolean success = scoreCalculator.isMissionSuccess(itemResult.getMissionType(), itemResult, missionItem);

            totalScore += score;
            if (!success) allSuccess = false;

            // 결과 메시지 빌더에 상태 추가
            switch (itemResult.getMissionType()) {
                case COIN -> msgBuilder.append(
                        success ? "코인을 하나도 빠짐없이 다 모았어요! 대단해요! " : "코인을 몇 개 놓쳤지만 잘했어요! "
                );
                case OBSTACLE -> msgBuilder.append(
                        success ? "장애물을 멋지게 피했어요! 집중력이 최고예요! " : "조금 부딪혔지만 끝까지 도전했어요! "
                );
                case PHOTO -> msgBuilder.append(
                        success ? "사진을 정확한 위치에서 잘 찍었어요! " : "조금 위치가 달랐지만 사진을 찍으려는 노력이 멋졌어요! "
                );
            }
        }

        // DroneMissionResult 엔티티 생성 및 저장
        DroneMissionResult result = DroneMissionResult.builder()
                .userId(userId)
                .mission(mission)
                .droneId(dto.getDroneId())
                .totalTime(itemResults.stream().mapToDouble(i -> i.getTotalTime()).sum())
                .deviationCount(itemResults.stream().mapToInt(i -> i.getDeviationCount()).sum())
                .collisionCount(itemResults.stream().mapToInt(i -> i.getCollisionCount()).sum())
                .score(totalScore)
                .status(allSuccess ? MissionResultStatus.SUCCESS : MissionResultStatus.FAIL)
                .build();

        DroneMissionResult saved = resultRepository.save(result);

        // 최종 안내 메시지 작성
        String finalMsg = allSuccess
                ? "모든 미션을 완벽하게 해냈어요! 정말 멋진 드론 조종자예요! "
                : "조금 어려웠지만 끝까지 포기하지 않았어요! 다음엔 더 잘할 수 있어요! ";
        finalMsg += "\n" + msgBuilder.toString();

        // 음성 출력용 메시지 정제
        // - 특수기호, 줄바꿈 등을 제거한 버전
        // - AI 음성 합성(TTS) 시스템에서 오류 없이 읽히도록 가공
        String cleanMsg = cleanForTTS(finalMsg);

        // 응답 DTO 반환
        return MissionCompleteResponseDto.builder()
                .score(totalScore)
                .duration(saved.getTotalTime())
                .deviationCount(saved.getDeviationCount())
                .collisionCount(saved.getCollisionCount())
                .success(allSuccess)
                .message(cleanMsg)
                .rawMessage(finalMsg)
                .build();
    }


    @Override
    public Optional<Mission> findById(Long id) {
        return missionRepository.findById(id);
    }

    @Override
    public Optional<MissionItem> findMissionItemById(Long id) {
        return missionItemRepository.findById(id);
    }

    /**
     * 미션 엔티티 직접 조회
     *
     * - 주어진 ID를 기반으로 Mission 엔티티를 조회한다.
     * - 해당 ID의 미션이 존재하지 않으면 예외를 발생시킨다.
     * - 컨트롤러 또는 서비스 내부에서 MissionResponseDto 변환 전에 엔티티가 직접 필요한 경우 사용된다.
     *
     * @param id 조회할 미션 ID
     * @return 조회된 Mission 엔티티
     * @throws IllegalArgumentException 미션이 존재하지 않을 경우
     */
    @Override
    public Mission getMissionEntity(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
    }

    /**
     * TTS(Text-To-Speech)용 문자열 정제 함수
     *
     * - 원본 안내 메시지에서 특수기호, 줄바꿈 등을 제거하여
     *   음성 합성에 적합한 단순하고 깔끔한 문장으로 변환한다.
     *
     * @param text 변환할 원본 문자열
     * @return 특수기호와 줄바꿈이 제거된 정제된 문자열
     */
    private String cleanForTTS(String text) {
        // 특수문자 중 . ! ? 는 허용하고 나머지만 제거
        return text.replaceAll("[^\\p{L}\\p{N}\\s.!?]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

}


