package com.mtvs.flykidsbackend.user.controller;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.mtvs.flykidsbackend.user.dto.UpdatePasswordRequestDto;
import com.mtvs.flykidsbackend.user.dto.UpdateNicknameRequestDto;

import java.util.Map;

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
     * - 사용자로부터 이메일, 비밀번호, 역할 정보를 받아 신규 회원 등록 처리
     * - 정상 시 201 Created 반환, 예외 시 400 Bad Request 반환
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
     * - 이메일과 비밀번호 검증 후 액세스 토큰과 리프레시 토큰 발급
     * - 성공 시 200 OK와 토큰 정보 반환, 실패 시 401 Unauthorized 반환
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
     * - 요청 헤더의 액세스 토큰에서 사용자 이름 추출 후 사용자 정보 반환
     * - Swagger 인증 시 정상 작동
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
     * - 인증된 사용자만 접근 가능
     * - 요청 바디에 닉네임 정보 포함, 닉네임 변경 처리
     * - 성공 시 200 OK, 실패 시 400 Bad Request 반환
     */
    @Operation(
            summary = "닉네임 수정",
            description = "현재 로그인한 사용자의 닉네임을 변경합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/nickname")
    public ResponseEntity<?> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateNicknameRequestDto dto) {
        if(userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = userDetails.getUsername();
        try {
            userService.updateNickname(username, dto.getNickname());
            return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 비밀번호 수정 API
     * - 인증된 사용자만 접근 가능
     * - 요청 바디에 새 비밀번호 포함, 비밀번호 변경 처리
     * - 성공 시 200 OK, 실패 시 400 Bad Request 반환
     */
    @Operation(
            summary = "비밀번호 수정",
            description = "현재 로그인한 사용자의 비밀번호를 변경합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody UpdatePasswordRequestDto dto) {
        if(userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = userDetails.getUsername();
        try {
            userService.updatePassword(username, dto.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 회원 탈퇴 API
     * - 인증된 사용자의 계정을 비활성화 처리(탈퇴)
     * - 정상 처리 시 200 OK 및 성공 메시지 반환
     * - 실패 시 400 Bad Request 및 오류 메시지 반환
     */
    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 계정을 비활성화(탈퇴 처리)합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> withdrawUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = userDetails.getUsername();
        try {
            userService.withdrawUser(username);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 아이디 중복 확인 API
     * - 쿼리 파라미터 ?username=aaa
     * - 사용 가능하면 {"available": true} 반환
     */
    @Operation(summary = "아이디 중복 확인",
            description = "회원가입 전에 아이디가 사용 가능한지 확인합니다.")
    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplicate(
            @RequestParam String username) {

        boolean available = userService.checkUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

}
