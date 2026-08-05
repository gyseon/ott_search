package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.LoginRequestDto;
import com.example.demo.domain.user.dto.SignUpRequestDto;
import com.example.demo.domain.user.dto.TokenResponseDto;
import com.example.demo.domain.user.dto.UserResponseDto;
import com.example.demo.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody SignUpRequestDto requestDto) {
        UserResponseDto response = userService.signUp(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        TokenResponseDto tokenResponse = userService.login(requestDto);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyInfo(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok("현재 로그인한 사용자의 ID는 [" + userId + "] 입니다!");
    }
}