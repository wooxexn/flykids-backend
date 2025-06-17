package com.mtvs.flykidsbackend.user.controller;


import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.SignupRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     * POST /api/users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 API
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginRequestDto requestDto) {
        TokenResponseDto tokenResponse = userService.login(requestDto);
        return ResponseEntity.ok(tokenResponse);
    }
}
