package com.mtvs.flykidsbackend.domain.user.service;

import com.mtvs.flykidsbackend.domain.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.domain.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.domain.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.domain.user.dto.UserInfoResponseDto;
import com.mtvs.flykidsbackend.domain.user.entity.User;

import java.util.Optional;

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
    Optional<User> findByUsername(String username);

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

    // 회원 탈퇴
    void withdrawUser(String username);

    /**
     * ID로 사용자 조회
     * @param id 사용자 ID
     * @return Optional<User>
     */
    Optional<User> findById(Long id);

    /**
     * 아이디 중복 여부 확인
     * - 주어진 아이디가 사용 가능한지 확인한다.
     * - 형식이 유효하지 않으면 예외 발생
     * - ACTIVE 상태의 사용자 중 중복 여부를 검사
     *
     * @param username 중복 확인할 사용자 아이디
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    boolean checkUsernameAvailable(String username);
}