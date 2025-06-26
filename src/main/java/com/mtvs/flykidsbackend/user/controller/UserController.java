package com.mtvs.flykidsbackend.user.controller;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.user.dto.*;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mtvs.flykidsbackend.user.dto.UpdatePasswordRequestDto;
import com.mtvs.flykidsbackend.user.dto.UpdateNicknameRequestDto;

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
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        try {
            userService.signup(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto requestDto) {
        try {
            TokenResponseDto tokenResponse = userService.login(requestDto);
            return ResponseEntity.ok(tokenResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * 내 정보 조회 API
     * - 요청 헤더의 Access Token에서 username을 추출하여 사용자 정보를 반환
     * - Swagger의 Authorize 버튼으로 토큰 입력 시 정상 작동
     *
     * @param request HttpServletRequest (헤더에서 토큰 추출용)
     * @return UserInfoResponseDto (username, nickname, role 포함)
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 반환합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDto> getMyInfo(
            @Parameter(hidden = true) HttpServletRequest request) {

        String token = jwtUtil.resolveToken(request);
        String username = jwtUtil.getUsername(token);

        UserInfoResponseDto userInfo = userService.getMyInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 닉네임 수정 API
     * PATCH /api/users/nickname
     */
    @Operation(
            summary = "닉네임 수정",
            description = "현재 로그인한 사용자의 닉네임을 변경합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/nickname")
    public ResponseEntity<?> updateNickname(HttpServletRequest request,
                                            @RequestBody UpdateNicknameRequestDto dto) {
        String username = jwtUtil.getUsername(jwtUtil.resolveToken(request));
        try {
            userService.updateNickname(username, dto.getNickname());
            return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 비밀번호 수정 API
     * PATCH /api/users/password
     */
    @Operation(
            summary = "비밀번호 수정",
            description = "현재 로그인한 사용자의 비밀번호를 변경합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(HttpServletRequest request,
                                            @RequestBody UpdatePasswordRequestDto dto) {
        String username = jwtUtil.getUsername(jwtUtil.resolveToken(request));
        try {
            userService.updatePassword(username, dto.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 회원 탈퇴 API
     * - 현재 로그인한 사용자의 계정을 비활성화(탈퇴 처리)한다.
     * - JWT 토큰에서 username을 추출하여 탈퇴 처리 대상 사용자로 지정
     * - 정상 처리 시 200 OK 및 성공 메시지 반환
     * - 예외 발생 시 400 Bad Request 및 메시지 반환
     *
     * @param request HTTP 요청 (JWT 토큰 포함)
     * @return 처리 결과 메시지 응답
     */
    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 계정을 비활성화(탈퇴 처리)합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> withdrawUser(HttpServletRequest request) {
        String username = jwtUtil.getUsername(jwtUtil.resolveToken(request));
        try {
            userService.withdrawUser(username);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
