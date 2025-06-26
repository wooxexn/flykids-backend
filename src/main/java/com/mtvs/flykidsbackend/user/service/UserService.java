package com.mtvs.flykidsbackend.user.service;

import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.dto.UserInfoResponseDto;
import com.mtvs.flykidsbackend.user.entity.User;

/**
 * 사용자 관련 서비스 로직의 인터페이스
 * 회원가입, 로그인, 사용자 조회 기능을 정의한다
 */
public interface UserService {

    /**
     * 회원가입 기능
     * 전달받은 요청 DTO를 바탕으로 사용자 정보를 저장한다
     *
     * @param requestDto 회원가입 요청 데이터 (username, password 등 포함)
     */
    void signup(SignupRequestDto requestDto);

    /**
     * 로그인 기능
     * 요청된 로그인 정보가 유효하면 액세스/리프레시 토큰을 반환한다
     *
     * @param requestDto 로그인 요청 데이터 (username, password)
     * @return 토큰 응답 DTO (accessToken, refreshToken 포함)
     */
    TokenResponseDto login(LoginRequestDto requestDto);

    /**
     * 사용자 정보 조회
     * 사용자명을 기준으로 DB에서 사용자 정보를 조회한다
     *
     * @param username 사용자 아이디
     * @return 조회된 사용자 엔티티 (없을 경우 예외 발생 가능)
     */
    User findByUsername(String username);

    /**
     * 현재 로그인한 사용자의 정보를 조회한다.
     *
     * @param username 토큰에서 추출한 로그인 ID
     * @return UserInfoResponseDto
     */
    UserInfoResponseDto getMyInfo(String username);

    // 닉네임 변경
    void updateNickname(String username, String newNickname);

    // 비밀번호 변경
    void updatePassword(String username, String newPassword);
}