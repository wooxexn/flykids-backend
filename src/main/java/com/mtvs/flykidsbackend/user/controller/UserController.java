package com.mtvs.flykidsbackend.user.controller;


import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.dto.UserInfoResponseDto;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "사용자",
        description = "회원가입, 로그인, 마이페이지(정보 조회/닉네임·비밀번호 수정/회원 탈퇴) API"
)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 API
     * POST /api/users/signup
     */
    @Operation(
            summary = "회원가입",
            description = "사용자로부터 이메일, 비밀번호, 역할 정보를 받아 회원가입을 처리합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 API
     * POST /api/users/login
     */
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증한 후, access/refresh 토큰을 반환합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginRequestDto requestDto) {
        TokenResponseDto tokenResponse = userService.login(requestDto);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 내 정보 조회 API
     * - 요청 헤더의 Access Token에서 username을 추출하여 사용자 정보를 반환
     * - Swagger의 Authorize 버튼으로 토큰 입력 시 정상 작동
     *
     * @param request HttpServletRequest (헤더에서 토큰 추출용)
     * @return UserInfoResponseDto (username, nickname, role 포함)
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDto> getMyInfo(
            @Parameter(hidden = true) HttpServletRequest request) {

        String token = jwtUtil.resolveToken(request);
        String username = jwtUtil.getUsername(token);

        UserInfoResponseDto userInfo = userService.getMyInfo(username);
        return ResponseEntity.ok(userInfo);
    }

}
