package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.LoginRequestDto;
import com.example.demo.domain.user.dto.SignUpRequestDto;
import com.example.demo.domain.user.dto.TokenResponseDto;
import com.example.demo.domain.user.dto.UserResponseDto;
import com.example.demo.domain.user.entity.Role;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider; // 💡 필드 및 생성자 주입 추가!

    @Transactional
    public UserResponseDto signUp(SignUpRequestDto requestDto) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + requestDto.getEmail());
        }

        // 2. 비밀번호 암호화 (BCrypt)
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. User 엔티티 생성
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .role(Role.USER)
                .build();

        // 4. 저장 후 응답 DTO 반환
        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }


    @Transactional
    public TokenResponseDto login(LoginRequestDto requestDto) {
        // 1. 이메일 존재 여부 확인
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다: " + requestDto.getEmail()));

        // 2. 비밀번호 일치 여부 확인 (BCrypt)
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 토큰 생성
        String accessToken = jwtProvider.createToken(user.getId(), user.getEmail(), user.getRole().name());

        return new TokenResponseDto("Bearer", accessToken);
    }
}