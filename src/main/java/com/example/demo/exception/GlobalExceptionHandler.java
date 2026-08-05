package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("💥 CustomException 발생: [{}] {}", ex.getCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .status(ex.getHttpStatus().value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // ✨ 정적 리소스(favicon.ico 등)를 찾지 못했을 때 발생하는 예외 처리 (로그 소음 방지)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        // ERROR 대신 WARN/DEBUG 로그로 간단히 처리
        log.warn("🔍 정적 리소스를 찾을 수 없음: {}", ex.getResourcePath());

        ErrorResponse response = ErrorResponse.builder()
                .code("RESOURCE_NOT_FOUND")
                .message("요청하신 정적 리소스를 찾을 수 없습니다.")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 2. 그 외 모든 알 수 없는 예외 처리 (Catch-All)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("💥 Unexpected Server Exception: ", ex);

        ErrorResponse response = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}