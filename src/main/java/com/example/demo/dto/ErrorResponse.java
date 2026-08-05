package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String code;          // 예외 코드 (예: TMDB_API_ERROR, INTERNAL_SERVER_ERROR)
    private final String message;       // 사용자 친화적 에러 메시지
    private final int status;           // HTTP 상태 코드 (400, 404, 500 등)
    private final LocalDateTime timestamp; // 에러 발생 시각
}