package com.mtvs.flykidsbackend.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserMissionProgressController 통합 테스트 (단일 미션 구조용)
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
public class UserMissionProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private Long missionId;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 로그인해서 accessToken 획득
        LoginRequestDto loginDto = new LoginRequestDto("tester1", "tester1234");
        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 2. 단일 미션 생성
        MissionRequestDto missionRequestDto = MissionRequestDto.builder()
                .title("단일 미션 테스트")
                .timeLimit(180)
                .type(MissionType.COIN)
                .totalCoinCount(10)
                .build();

        String missionJson = objectMapper.writeValueAsString(missionRequestDto);

        String missionResponse = mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missionJson)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        missionId = objectMapper.readTree(missionResponse).get("id").asLong();
    }

    @Test
    void getProgressList_success() throws Exception {
        mockMvc.perform(get("/api/user-mission-progress/missions/{missionId}", missionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateProgress_success() throws Exception {
        // 단일 미션용으로 수정된 경로에 맞춰서 상태 업데이트
        mockMvc.perform(post("/api/user-mission-progress/missions/{missionId}",
                        missionId)
                        .param("status", "COMPLETED")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // 업데이트 상태 반영 확인 (선택사항)
        MvcResult result = mockMvc.perform(get("/api/user-mission-progress/missions/{missionId}", missionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        assertThat(responseStr).contains("COMPLETED");
    }
}
