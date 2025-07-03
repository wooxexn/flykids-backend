package com.mtvs.flykidsbackend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void signupAndLogin() throws Exception {
        SignupRequestDto signupDto = new SignupRequestDto("tester", "tester5678", "테스터용");
        var signupResult = mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andReturn();

        int status = signupResult.getResponse().getStatus();
        if (status != 201 && status != 400) {
            // 201 Created 혹은 400 Bad Request(중복)만 허용, 그 외는 예외 발생
            throw new RuntimeException("회원가입 실패 상태: " + status);
        }

        // 로그인 진행
        LoginRequestDto loginDto = new LoginRequestDto("tester", "tester5678");
        String response = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponseDto tokenResponse = objectMapper.readValue(response, TokenResponseDto.class);
        accessToken = tokenResponse.getAccessToken();
    }


    @Test
    void signup_성공() throws Exception {
        SignupRequestDto dto = new SignupRequestDto("tester3", "tester5678", "테스터3");
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("회원가입이 완료되었습니다."));
    }

    @Test
    void getMyInfo_성공() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    void updateNickname_성공() throws Exception {
        UpdateNicknameRequestDto dto = new UpdateNicknameRequestDto("변경닉네임");
        mockMvc.perform(patch("/api/users/nickname")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("닉네임이 성공적으로 변경되었습니다."));
    }

    @Test
    void updatePassword_성공() throws Exception {
        // 기존 비밀번호 "tester1234"에서 새 비밀번호 "tester5678"로 변경
        UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("tester1234", "tester5678");
        mockMvc.perform(patch("/api/users/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호가 성공적으로 변경되었습니다."));
    }

    @Test
    void withdrawUser_성공() throws Exception {
        mockMvc.perform(delete("/api/users/withdraw")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 탈퇴가 완료되었습니다."));
    }
}
