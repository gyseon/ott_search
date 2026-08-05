package com.example.demo.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Request Header에서 Bearer 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검증 및 SecurityContext 등록
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            Long userId = jwtProvider.getUserId(token);

            // 인증 객체 생성
            UserAuthenticationToken authentication = new UserAuthenticationToken(userId, Collections.emptyList());

            // SecurityContext에 인증 정보 저장 (이후 컨트롤러에서 사용자 조회 가능)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // Header에서 "Bearer " 접두어 제거 후 토큰만 반환
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}