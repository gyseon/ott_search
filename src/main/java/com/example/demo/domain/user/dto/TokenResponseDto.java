package com.example.demo.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {

    private String grantType; // 보통 "Bearer" 사용
    private String accessToken;
}