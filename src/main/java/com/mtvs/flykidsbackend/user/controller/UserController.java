package com.mtvs.flykidsbackend.user.controller;


import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
