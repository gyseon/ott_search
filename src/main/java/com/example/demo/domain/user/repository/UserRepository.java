package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 회원 존재 여부 확인 (중복 가입 방지)
    boolean existsByEmail(String email);

    // 이메일로 회원 조회 (로그인 시 활용)
    Optional<User> findByEmail(String email);
}