package com.mtvs.flykidsbackend.user.service;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.dto.UserInfoResponseDto;
import com.mtvs.flykidsbackend.user.entity.Role;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 처리
     * - 아이디 중복 확인
     * - 비밀번호 형식 검증
     * - 닉네임 길이 검증
     * - 사용자 정보 저장
     */
    @Override
    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();
        String nickname = requestDto.getNickname();

        // 아이디 유효성 검사
        if (username.length() < 5 || username.length() > 20) {
            throw new IllegalArgumentException("아이디는 5자 이상 20자 이하로 입력해주세요.");
        }

        // 아이디 형식 검사 (영문자‧숫자만 허용, 특수문자 사용 불가)
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException(
                    "아이디는 영문자, 숫자만 가능하고 특수문자는 사용할 수 없습니다."
            );
        }

        // 아이디 중복 체크
        if (userRepository.existsByUsernameAndStatus(username, User.UserStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 유효성 검사
        validatePassword(password);

        // 닉네임 길이 제한 검사
        if (nickname.length() < 2 || nickname.length() > 12) {
            throw new IllegalArgumentException("닉네임은 2자 이상 12자 이하로 입력해주세요.");
        }

        // 사용자 저장 (비밀번호 암호화 및 기본 권한 USER 설정)
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(Role.USER)
                .status(User.UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인 처리
     * - 아이디로 사용자 조회
     * - 비밀번호 일치 여부 확인
     * - 액세스 토큰과 리프레시 토큰 생성 및 반환
     */
    @Override
    public TokenResponseDto login(LoginRequestDto requestDto) {
        // 아이디 존재 여부 확인
        User user = userRepository.findByUsernameAndStatus(requestDto.getUsername(), User.UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole().name());

        // 리프레시 토큰 생성
        String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

        // 두 토큰을 묶어 반환
        return new TokenResponseDto(accessToken, refreshToken);
    }

    /**
     * 비밀번호 유효성 검사
     * - 8자 이상
     * - 영문자 1자 이상 포함
     * - 숫자 1자 이상 포함
     */
    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("비밀번호에는 영문자가 최소 1자 이상 포함되어야 합니다.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("비밀번호에는 숫자가 최소 1자 이상 포함되어야 합니다.");
        }
    }

    /**
     * 사용자 이름(username)으로 사용자 정보 조회
     * 로그인 등의 인증 과정에서 내부적으로 사용된다
     *
     * @param username 조회할 사용자 아이디 (예: 이메일)
     * @return User 엔티티 객체 반환
     * @throws UsernameNotFoundException 사용자가 존재하지 않을 경우 예외 발생
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 현재 로그인한 사용자의 정보를 조회한다.
     * - username과 상태가 ACTIVE인 사용자만 조회한다.
     * - 해당 사용자가 없으면 IllegalArgumentException 예외를 발생시킨다.
     *
     * @param username 조회할 사용자 아이디
     * @return UserInfoResponseDto 사용자 정보 DTO (username, nickname, role 포함)
     * @throws IllegalArgumentException 사용자가 존재하지 않을 경우 예외 발생
     */
    @Override
    public UserInfoResponseDto getMyInfo(String username) {
        // 사용자 조회 (없으면 예외 발생)
        User user = userRepository.findByUsernameAndStatus(username, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 응답 DTO 변환
        return new UserInfoResponseDto(
                user.getUsername(),
                user.getNickname(),
                user.getRole().name()
        );
    }

    /**
     * 사용자 닉네임 수정
     * - username으로 사용자를 조회하고, 전달받은 새로운 닉네임으로 업데이트
     *
     * @param username 사용자 아이디
     * @param newNickname 변경할 닉네임
     */
    @Override
    public void updateNickname(String username, String newNickname) {

        // 1) 닉네임 길이 검증
        if (newNickname.length() < 2 || newNickname.length() > 12) {
            throw new IllegalArgumentException("닉네임은 2자 이상 12자 이하로 입력해주세요.");
        }

        // 2) 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 3) 기존 닉네임과 동일한지 검사
        if (user.getNickname().equals(newNickname)) {
            throw new IllegalArgumentException("새 닉네임이 기존 닉네임과 동일합니다.");
        }

        // 4) 다른 사용자가 이미 사용 중인지 검사
        if (userRepository.existsByNicknameAndStatus(newNickname, User.UserStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 5) 닉네임 업데이트 후 저장
        user.updateNickname(newNickname);
        userRepository.save(user);
    }

    /**
     * 사용자 비밀번호 수정
     * - username으로 사용자를 조회하고, 전달받은 새로운 비밀번호를 암호화하여 업데이트
     *
     * @param username 사용자 아이디
     * @param newPassword 변경할 비밀번호 (평문)
     */
    @Override
    public void updatePassword(String username, String newPassword) {

        // 새 비밀번호 형식 검사 (8자 이상, 영문·숫자 포함)
        validatePassword(newPassword);

        User user = userRepository.findByUsernameAndStatus(username, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 동일합니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * 회원 탈퇴 처리
     * - username으로 사용자를 조회하고, 상태를 INACTIVE로 변경하여 탈퇴 처리
     * - 이미 탈퇴된 사용자인 경우 예외 발생
     *
     * @param username 탈퇴 처리할 사용자 아이디
     * @throws IllegalArgumentException 사용자가 존재하지 않을 경우
     * @throws IllegalStateException 이미 탈퇴된 사용자일 경우
     */
    @Override
    public void withdrawUser(String username) {
        User user = userRepository.findByUsernameAndStatus(username, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        if (user.getStatus() == User.UserStatus.INACTIVE) {
            throw new IllegalStateException("이미 탈퇴 처리된 사용자입니다.");
        }

        // 상태를 INACTIVE로 변경하고 탈퇴 시간 기록
        user.withdraw();

        // 변경된 사용자 정보 저장
        userRepository.save(user);
    }

    /**
     * 사용자 ID로 사용자 조회
     * - 주어진 사용자 ID로 DB에서 사용자 정보를 조회한다.
     * - 존재하지 않을 경우 Optional.empty() 반환
     *
     * @param id 조회할 사용자 ID
     * @return Optional<User> 조회된 사용자 (없을 경우 empty 반환)
     */
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 아이디 사용 가능 여부 확인
     * - 입력된 아이디(username)의 형식이 올바른지 검증
     * - 현재 ACTIVE 상태의 사용자 중 동일 아이디 존재 여부를 조회하여 사용 가능 여부 반환
     *
     * @param username 확인할 아이디
     * @return true: 사용 가능 / false: 이미 사용 중
     * @throws IllegalArgumentException 아이디 형식이 잘못된 경우 (길이 또는 정규식 불일치)
     */
    @Override
    public boolean checkUsernameAvailable(String username) {

        // 1. 아이디 형식 유효성 검사 (5~20자, 영문자+숫자만 허용)
        if (username.length() < 5 || username.length() > 20
                || !username.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("아이디 형식이 올바르지 않습니다.");
        }

        // 2. 현재 사용 중인 ACTIVE 상태의 동일 아이디가 존재하는지 확인
        return !userRepository.existsByUsernameAndStatus(
                username, User.UserStatus.ACTIVE);
    }

}