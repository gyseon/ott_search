package com.example.demo.global.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;

    public UserAuthenticationToken(Long userId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        setAuthenticated(true); // 인증 완료 처리
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.userId; // 컨트롤러 등에서 로그인 사용자 ID로 조회할 수 있음
    }
}