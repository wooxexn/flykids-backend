package com.mtvs.flykidsbackend.user.service;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.entity.Role;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        // 아이디 중복 체크
        if (userRepository.existsByUsername(username)) {
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
        // 아이디 존재 여부 확인, 없으면 BadCredentialsException 던짐
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("존재하지 않는 아이디입니다."));

        // 비밀번호 확인, 틀리면 BadCredentialsException 던짐
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
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
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

}