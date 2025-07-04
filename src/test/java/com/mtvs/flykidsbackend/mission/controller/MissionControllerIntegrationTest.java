package com.mtvs.flykidsbackend.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto.MissionItemDto;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MissionController 통합 테스트 클래스
 * - 실제 서비스 환경과 유사하게 MockMvc를 사용하여 API 엔드포인트를 테스트함
 * - 미션 등록, 수정, 조회, 삭제 기능에 대한 검증 수행
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;  // MockMvc 객체 주입

    @Autowired
    private ObjectMapper objectMapper;  // JSON 직렬화/역직렬화 도구

    private MissionRequestDto missionRequestDto;  // 테스트용 미션 요청 DTO

    private Long createdMissionId;  // 생성된 미션 ID 저장용

    private String accessToken;

    /**
     * 각 테스트 실행 전 호출됨
     * 테스트에 사용할 기본 미션 요청 DTO를 초기화
     */
    @BeforeEach
    void setUp() throws Exception {
        // 로그인 DTO (실제 테스트용 사용자 정보)
        LoginRequestDto loginDto = new LoginRequestDto("tester1", "tester1234");

        // 로그인 API 호출해서 JWT 토큰 발급받기
        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponseDto tokenResponse = objectMapper.readValue(loginResponse, TokenResponseDto.class);
        accessToken = tokenResponse.getAccessToken();

        missionRequestDto = MissionRequestDto.builder()
                .title("테스트 미션")
                .items(List.of(
                        MissionItemDto.builder()
                                .title("코인 미션")
                                .timeLimit(300)
                                .type(MissionType.COIN)
                                .totalCoinCount(10)
                                .build(),
                        MissionItemDto.builder()
                                .title("장애물 피하기")
                                .timeLimit(300)
                                .type(MissionType.OBSTACLE)
                                .totalCoinCount(0)
                                .build(),
                        MissionItemDto.builder()
                                .title("사진 찍기")
                                .timeLimit(200)
                                .type(MissionType.PHOTO)
                                .totalCoinCount(0)
                                .build()
                ))
                .build();
    }

    /**
     * 미션 등록 API 성공 케이스 테스트
     * - POST /api/missions 호출 후 상태 코드 200, 응답에 제목과 아이템 갯수 확인
     * - 응답에서 미션 ID 추출하여 이후 테스트에서 재사용
     */
    @Test
    void createMission_success() throws Exception {
        String json = objectMapper.writeValueAsString(missionRequestDto);

        String response = mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())  // HTTP 200 OK 기대
                .andExpect(jsonPath("$.title").value("테스트 미션"))  // 제목 검증
                .andExpect(jsonPath("$.items", hasSize(3)))  // 미션 아이템 개수 검증
                .andExpect(jsonPath("$.items[*].type", hasItems("COIN", "OBSTACLE", "PHOTO"))) // 미션 유형 검증
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 응답 JSON에서 생성된 미션 ID 추출 후 필드에 저장
        createdMissionId = objectMapper.readTree(response).get("id").asLong();
    }

    /**
     * 미션 수정 API 성공 케이스 테스트
     * - 기존 미션 생성 후 PATCH /api/missions/{id} 호출
     * - 응답 상태 코드 200, 제목 및 아이템 개수, 타입 검증
     */
    @Test
    void updateMission_success() throws Exception {
        // 미션 생성 선행
        createMission_success();

        MissionRequestDto updateDto = MissionRequestDto.builder()
                .title("수정된 미션")
                .items(List.of(
                        MissionItemDto.builder()
                                .title("사진 찍기")
                                .timeLimit(200)
                                .type(MissionType.PHOTO)
                                .totalCoinCount(0)
                                .build()
                ))
                .build();

        String json = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch("/api/missions/{id}", createdMissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())  // HTTP 200 OK 기대
                .andExpect(jsonPath("$.title").value("수정된 미션"))  // 제목 변경 확인
                .andExpect(jsonPath("$.items", hasSize(1)))  // 아이템 개수 변경 확인
                .andExpect(jsonPath("$.items[0].type").value("PHOTO"));  // 아이템 타입 확인
    }

    /**
     * 단일 미션 조회 API 성공 케이스 테스트
     * - 미션 생성 후 GET /api/missions/{id} 호출
     * - 응답 상태 200, ID 및 제목 값 검증
     */
    @Test
    void getMission_success() throws Exception {
        // 미션 생성 선행
        createMission_success();

        mockMvc.perform(get("/api/missions/{id}", createdMissionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())  // HTTP 200 OK 기대
                .andExpect(jsonPath("$.id").value(createdMissionId))  // ID 검증
                .andExpect(jsonPath("$.title").value("테스트 미션"));  // 제목 검증
    }

    /**
     * 전체 미션 목록 조회 API 성공 케이스 테스트
     * - 미션 생성 후 GET /api/missions 호출
     * - 응답 상태 200, 결과가 비어있지 않고 생성된 미션 제목 포함 여부 검증
     */
    @Test
    void getAllMissions_success() throws Exception {
        // 미션 생성 선행
        createMission_success();

        mockMvc.perform(get("/api/missions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())  // HTTP 200 OK 기대
                .andExpect(jsonPath("$", not(empty())))  // 리스트 비어있지 않음
                .andExpect(jsonPath("$[*].title", hasItem("테스트 미션")));  // 제목 포함 확인
    }

    /**
     * 미션 삭제 API 성공 케이스 테스트
     * - 미션 생성 후 DELETE /api/missions/{id} 호출
     * - 응답 상태 204 No Content 확인
     * - 삭제된 미션 조회 시 400 Bad Request 또는 404 Not Found 예상
     */
    @Test
    void deleteMission_success() throws Exception {
        // 미션 생성 선행
        createMission_success();

        // 미션 삭제 요청
        mockMvc.perform(delete("/api/missions/{id}", createdMissionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // 삭제 후 미션 조회 → 실제 상태 코드 출력 및 검증
        MvcResult result = mockMvc.perform(get("/api/missions/{id}", createdMissionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();

        System.out.println("💥 삭제 후 조회 응답 코드: " + result.getResponse().getStatus());

        assertEquals(404, result.getResponse().getStatus());


    }


}
