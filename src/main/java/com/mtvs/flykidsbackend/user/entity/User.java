package com.mtvs.flykidsbackend.user.entity;


import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티 클래스
 * - 로그인 아이디(username), 비밀번호(password), 닉네임(nickname), 권한(role) 포함
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @Column(nullable = false, unique = true)
    private String username; // 로그인 아이디 (중복 불가)

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(nullable = false)
    private String nickname; // 사용자 닉네임 (화면에 표시용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 사용자 권한 (USER / ADMIN)

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
