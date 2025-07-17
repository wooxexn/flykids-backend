package com.mtvs.flykidsbackend.domain.user.controller;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.domain.user.dto.*;
import com.mtvs.flykidsbackend.user.dto.*;
import com.mtvs.flykidsbackend.domain.user.service.UserService;
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
     * - 사용자가 아이디(username), 비밀번호, 닉네임 정보를 입력하여 신규 회원 가입을 요청할 때 호출한다.
     * - 입력값 검증 및 중복 체크 후 사용자 정보를 저장한다.
     * - 가입 성공 시 HTTP 201 상태 코드와 성공 메시지를 반환한다.
     * - 입력값이 유효하지 않거나 중복 아이디/닉네임 등이 있을 경우 HTTP 400 상태 코드와 오류 메시지를 반환한다.
     */
    @Operation(
            summary = "회원가입",
            description = "사용자로부터 아이디, 비밀번호, 닉네임을 받아 신규 회원을 등록합니다. " +
                    "입력값 검증과 중복 체크를 수행하며, 성공 시 201 Created를 반환합니다."
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
     * - 아이디와 비밀번호를 검증한 후 액세스 토큰과 리프레시 토큰을 발급합니다.
     * - 성공 시 200 OK와 토큰 정보를 반환하며, 실패 시 401 Unauthorized를 반환합니다.
     */
    @Operation(
            summary = "로그인",
            description = "사용자가 아이디와 비밀번호를 입력하여 로그인할 때 호출합니다. " +
                    "입력 정보를 검증하고, 성공 시 인증에 필요한 액세스 및 리프레시 토큰을 반환합니다. " +
                    "이를 통해 이후 API 접근 시 인증이 가능하도록 지원합니다."
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
     * - 현재 로그인한 사용자가 자신의 정보를 확인하고자 할 때 호출합니다.
     * - 요청 헤더에 포함된 액세스 토큰에서 사용자 이름을 추출하여 해당 사용자의 정보를 반환합니다.
     * - Swagger 인증 환경에서도 정상적으로 작동합니다.
     */
    @Operation(
            summary = "내 정보 조회",
            description = "로그인된 사용자가 자신의 프로필 정보를 조회할 때 사용합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
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
     * - 사용자가 로그인 후 자신의 닉네임을 변경하고자 할 때 호출합니다.
     * - 요청 바디에 포함된 새 닉네임으로 변경 처리를 수행합니다.
     * - 변경이 성공하면 200 OK와 성공 메시지를 반환하며, 유효성 검사 실패 시 400 Bad Request를 반환합니다.
     */
    @Operation(
            summary = "닉네임 수정",
            description = "로그인한 사용자의 닉네임을 새로운 값으로 변경합니다. " +
                    "인증된 사용자만 접근 가능하며, 닉네임 중복이나 길이 제한 등 유효성 검사가 포함됩니다.",
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
     * - 로그인한 사용자가 자신의 비밀번호를 변경하고자 할 때 호출합니다.
     * - 요청 바디에 포함된 새 비밀번호를 받아 유효성 검사 후 암호화하여 저장합니다.
     * - 변경 성공 시 200 OK와 성공 메시지를 반환하며, 유효하지 않은 요청일 경우 400 Bad Request를 반환합니다.
     */
    @Operation(
            summary = "비밀번호 수정",
            description = "인증된 사용자가 자신의 비밀번호를 새 비밀번호로 변경합니다. " +
                    "기존 비밀번호와 동일하지 않아야 하며, 비밀번호 정책에 따라 검증이 이루어집니다.",
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
     * - 로그인한 사용자가 자신의 계정을 비활성화(탈퇴) 처리할 때 호출합니다.
     * - 계정 상태를 변경하여 더 이상 서비스 이용이 불가능하도록 만듭니다.
     * - 정상 처리 시 200 OK와 성공 메시지를 반환하며, 문제 발생 시 400 Bad Request를 반환합니다.
     */
    @DeleteMapping("/withdraw")
    @Operation(
            summary = "회원 탈퇴",
            description = "인증된 사용자가 자신의 계정을 비활성화하여 서비스 이용을 종료합니다. " +
                    "탈퇴 처리 후에는 계정이 비활성화되어 로그인 및 서비스 이용이 제한됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
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
     * - 회원가입 시 입력한 아이디가 이미 사용 중인지 사전 검증할 때 호출합니다.
     * - 쿼리 파라미터로 전달된 아이디의 사용 가능 여부를 반환합니다.
     * - 사용 가능한 아이디면 {"available": true}, 이미 사용 중이면 {"available": false}를 응답합니다.
     */
    @Operation(
            summary = "아이디 중복 확인",
            description = "회원가입 전에 입력한 아이디의 중복 여부를 확인하여 사용 가능한지 알려줍니다."
    )
    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplicate(
            @RequestParam String username) {

        boolean available = userService.checkUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

}
