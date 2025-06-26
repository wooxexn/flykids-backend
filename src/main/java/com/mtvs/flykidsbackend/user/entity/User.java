package com.mtvs.flykidsbackend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티 클래스
 * - 로그인 아이디(username), 비밀번호(password), 닉네임(nickname), 권한(role), 상태(status) 포함
 * - 소프트 딜리트(탈퇴) 구현: 상태와 탈퇴 시간 관리
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // 사용자 상태 (활성, 비활성)

    private LocalDateTime deletedAt;  // 탈퇴(비활성화) 시점 기록

    /**
     * 사용자 상태 값 (활성/비활성)
     */
    public enum UserStatus {
        ACTIVE,     // 활성 사용자
        INACTIVE    // 비활성 사용자(탈퇴 처리)
    }

    /**
     * 회원 탈퇴 처리 (소프트 딜리트)
     * - 상태를 INACTIVE로 변경
     * - 탈퇴 시점 현재 시간 기록
     */
    public void withdraw() {
        this.status = UserStatus.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 닉네임 수정
     * @param nickname 변경할 닉네임
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 비밀번호 수정
     * @param encodedPassword 암호화된 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
