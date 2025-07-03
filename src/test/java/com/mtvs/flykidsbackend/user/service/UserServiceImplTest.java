package com.mtvs.flykidsbackend.user.service;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.entity.Role;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl에 대한 단위 테스트 클래스
 * - 회원가입, 로그인, 닉네임/비밀번호 수정, 회원 탈퇴 기능 검증
 * - Mockito를 통한 의존성 모킹과 동작 검증 수행
 */
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private UserServiceImpl userService;

    /**
     * 각 테스트 시작 전 Mockito 어노테이션 초기화 수행
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------------ signup() 관련 테스트 ------------------------

    /**
     * 회원가입 정상 흐름 테스트
     * - 중복 아이디 없음
     * - 비밀번호 인코딩 정상 수행
     * - 저장된 User 객체 필드 값 검증
     */
    @Test
    void signup_정상적으로회원가입된다() {
        SignupRequestDto request = new SignupRequestDto("testuser", "Password123", "테스터");

        when(userRepository.existsByUsernameAndStatus("testuser", User.UserStatus.ACTIVE)).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");

        userService.signup(request);

        // 저장 시 User 객체 캡처
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getNickname()).isEqualTo("테스터");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    /**
     * 아이디 길이가 너무 짧을 경우 예외 발생 확인
     */
    @Test
    void signup_아이디가짧으면_예외() {
        assertThatThrownBy(() ->
                userService.signup(new SignupRequestDto("ab", "Password123", "닉네임"))
        ).hasMessageContaining("아이디는 5자 이상");
    }

    /**
     * 아이디에 허용되지 않는 문자가 포함된 경우 예외 발생 확인
     */
    @Test
    void signup_아이디형식이잘못되면_예외() {
        assertThatThrownBy(() ->
                userService.signup(new SignupRequestDto("invalid@", "Password123", "닉네임"))
        ).hasMessageContaining("영문자, 숫자만");
    }

    /**
     * 이미 활성 상태인 중복 아이디 존재 시 예외 발생 확인
     */
    @Test
    void signup_중복아이디면_예외() {
        when(userRepository.existsByUsernameAndStatus("testuser", User.UserStatus.ACTIVE)).thenReturn(true);
        assertThatThrownBy(() ->
                userService.signup(new SignupRequestDto("testuser", "Password123", "닉네임"))
        ).hasMessageContaining("이미 사용 중인 아이디");
    }

    /**
     * 비밀번호가 너무 짧을 경우 예외 발생 확인
     */
    @Test
    void signup_비밀번호가짧으면_예외() {
        assertThatThrownBy(() ->
                userService.signup(new SignupRequestDto("testuser", "short", "닉네임"))
        ).hasMessageContaining("비밀번호는 8자 이상");
    }

    /**
     * 닉네임이 너무 짧을 경우 예외 발생 확인
     */
    @Test
    void signup_닉네임짧으면_예외() {
        assertThatThrownBy(() ->
                userService.signup(new SignupRequestDto("testuser", "Password123", "ㅋ"))
        ).hasMessageContaining("닉네임은 2자 이상");
    }

    // ------------------------ login() 관련 테스트 ------------------------

    /**
     * 로그인 정상 흐름 테스트
     * - 존재하는 활성 사용자 아이디
     * - 비밀번호 일치
     * - JWT 토큰 정상 생성
     */
    @Test
    void login_정상적으로로그인된다() {
        String username = "testuser";
        String rawPwd = "Password123";
        String encPwd = "encodedPwd";

        User user = User.builder().username(username).password(encPwd)
                .nickname("테스터").role(Role.USER).status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsernameAndStatus(username, User.UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPwd, encPwd)).thenReturn(true);
        when(jwtUtil.createAccessToken(username, Role.USER.name())).thenReturn("access-token");
        when(jwtUtil.createRefreshToken(username)).thenReturn("refresh-token");

        TokenResponseDto res = userService.login(new LoginRequestDto(username, rawPwd));
        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
    }

    /**
     * 존재하지 않는 아이디로 로그인 시도 시 예외 발생 확인
     */
    @Test
    void login_존재하지않는아이디면_예외() {
        when(userRepository.findByUsernameAndStatus("nouser", User.UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.login(new LoginRequestDto("nouser", "1234"))
        ).hasMessageContaining("존재하지 않는 아이디");
    }

    /**
     * 비밀번호가 틀린 경우 예외 발생 확인
     */
    @Test
    void login_비밀번호틀리면_예외() {
        User user = User.builder().username("user").password("encodedPwd")
                .nickname("테스터").role(Role.USER).status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsernameAndStatus("user", User.UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPwd")).thenReturn(false);

        assertThatThrownBy(() ->
                userService.login(new LoginRequestDto("user", "wrong"))
        ).hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    // ------------------------ updateNickname() 관련 테스트 ------------------------

    /**
     * 닉네임 정상 변경 테스트
     * - 닉네임 중복 없음
     * - 정상적으로 User 엔티티의 닉네임이 변경됨
     */
    @Test
    void updateNickname_정상수정된다() {
        User user = User.builder().username("user").nickname("기존닉").status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndStatus("새닉", User.UserStatus.ACTIVE)).thenReturn(false);

        userService.updateNickname("user", "새닉");

        assertThat(user.getNickname()).isEqualTo("새닉");
    }

    /**
     * 닉네임 길이가 짧을 경우 예외 발생 확인
     */
    @Test
    void updateNickname_짧으면_예외() {
        assertThatThrownBy(() ->
                userService.updateNickname("user", "ㅋ")
        ).hasMessageContaining("닉네임은 2자 이상");
    }

    /**
     * 기존 닉네임과 동일한 닉네임으로 변경 시도 시 예외 발생 확인
     */
    @Test
    void updateNickname_기존과동일하면_예외() {
        User user = User.builder().username("user").nickname("같은닉").status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.updateNickname("user", "같은닉")
        ).hasMessageContaining("기존 닉네임과 동일");
    }

    /**
     * 중복된 닉네임으로 변경 시도 시 예외 발생 확인
     */
    @Test
    void updateNickname_중복닉네임이면_예외() {
        User user = User.builder().username("user").nickname("기존닉").status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndStatus("중복닉", User.UserStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() ->
                userService.updateNickname("user", "중복닉")
        ).hasMessageContaining("이미 사용 중인 닉네임");
    }

    // ------------------------ updatePassword() 관련 테스트 ------------------------

    /**
     * 비밀번호 정상 변경 테스트
     * - 기존 비밀번호와 다름
     * - 비밀번호 인코딩 정상 수행
     */
    @Test
    void updatePassword_정상수정된다() {
        User user = User.builder().username("user").password("oldEncoded").status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsernameAndStatus("user", User.UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("NewPass123", "oldEncoded")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("newEncoded");

        userService.updatePassword("user", "NewPass123");

        assertThat(user.getPassword()).isEqualTo("newEncoded");
    }

    /**
     * 비밀번호 유효성 검사 실패 시 예외 발생 확인
     */
    @Test
    void updatePassword_유효성실패_예외() {
        assertThatThrownBy(() ->
                userService.updatePassword("user", "short")
        ).hasMessageContaining("비밀번호는 8자 이상");
    }

    /**
     * 기존 비밀번호와 동일한 비밀번호로 변경 시도 시 예외 발생 확인
     */
    @Test
    void updatePassword_기존과같으면_예외() {
        User user = User.builder().username("user").password("encodedPwd").status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsernameAndStatus("user", User.UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SamePass1", "encodedPwd")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.updatePassword("user", "SamePass1")
        ).hasMessageContaining("기존 비밀번호와 동일");
    }

    // ------------------------ withdrawUser() 관련 테스트 ------------------------

    /**
     * 회원 정상 탈퇴 테스트
     * - 상태를 ACTIVE → INACTIVE로 변경
     */
    @Test
    void withdrawUser_정상탈퇴된다() {
        User user = User.builder().username("user").status(User.UserStatus.ACTIVE).build();
        when(userRepository.findByUsernameAndStatus("user", User.UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        userService.withdrawUser("user");

        assertThat(user.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
    }

    /**
     * 이미 탈퇴 처리된 회원에 대해 탈퇴 시도 시 예외 발생 확인
     */
    @Test
    void withdrawUser_이미탈퇴된유저_예외() {
        User user = User.builder().username("user").status(User.UserStatus.INACTIVE).build();
        when(userRepository.findByUsernameAndStatus("user", User.UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.withdrawUser("user")
        ).hasMessageContaining("이미 탈퇴");
    }
}
