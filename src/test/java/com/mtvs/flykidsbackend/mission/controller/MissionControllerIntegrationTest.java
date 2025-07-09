package com.mtvs.flykidsbackend.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MissionRequestDto missionRequestDto;

    private Long createdMissionId;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        // 로그인 DTO - 실제 테스트용 계정으로 변경 필요
        LoginRequestDto loginDto = new LoginRequestDto("tester1", "tester1234");

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
                .title("단일 미션 테스트")
                .timeLimit(180)
                .type(MissionType.COIN)  // 단일 미션 타입 지정
                .totalCoinCount(10)      // 코인 미션일 경우만 의미 있음
                .build();
    }

    @Test
    void createMission_success() throws Exception {
        String json = objectMapper.writeValueAsString(missionRequestDto);

        String response = mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("단일 미션 테스트"))
                .andExpect(jsonPath("$.type").value("COIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdMissionId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void updateMission_success() throws Exception {
        createMission_success();

        MissionRequestDto updateDto = MissionRequestDto.builder()
                .title("수정된 단일 미션")
                .timeLimit(200)
                .type(MissionType.PHOTO)
                .totalCoinCount(0)
                .build();

        String json = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch("/api/missions/{id}", createdMissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 단일 미션"))
                .andExpect(jsonPath("$.type").value("PHOTO"));
    }

    @Test
    void getMission_success() throws Exception {
        createMission_success();

        mockMvc.perform(get("/api/missions/{id}", createdMissionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdMissionId))
                .andExpect(jsonPath("$.title").value("단일 미션 테스트"));
    }

    @Test
    void getAllMissions_success() throws Exception {
        createMission_success();

        mockMvc.perform(get("/api/missions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].title", hasItem("단일 미션 테스트")));
    }

    @Test
    void deleteMission_success() throws Exception {
        createMission_success();

        mockMvc.perform(delete("/api/missions/{id}", createdMissionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/api/missions/{id}", createdMissionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();

        System.out.println("삭제 후 조회 응답 코드: " + result.getResponse().getStatus());

        assertEquals(404, result.getResponse().getStatus());
    }
}
