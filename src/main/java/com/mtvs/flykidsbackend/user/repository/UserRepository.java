package com.mtvs.flykidsbackend.user.repository;

import com.mtvs.flykidsbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 정보를 처리하는 JPA 리포지토리
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // username으로 사용자 찾기 (로그인 시 사용)
    Optional<User> findByUsername(String username);

    // username이 이미 존재하는지 여부 확인 (회원가입 중복 체크 시 사용 가능)
    boolean existsByUsername(String username);

    //주어진 닉네임이 이미 데이터베이스에 존재하는지 여부 확인
    boolean existsByNickname(String nickname);

    // 활성 상태인 사용자 중 username으로 사용자 찾기 (로그인 시 사용)
    Optional<User> findByUsernameAndStatus(String username, User.UserStatus status);

    // 활성 상태인 사용자 중 username이 이미 존재하는지 여부 확인 (회원가입 중복 체크 시)
    boolean existsByUsernameAndStatus(String username, User.UserStatus status);

    // 활성 상태인 사용자 중 주어진 닉네임이 이미 존재하는지 여부 확인
    boolean existsByNicknameAndStatus(String nickname, User.UserStatus status);
}