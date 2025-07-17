package com.mtvs.flykidsbackend.config.security;

import com.mtvs.flykidsbackend.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 스프링 시큐리티 UserDetails 인터페이스 구현체
 * - 인증 정보 및 권한 정보를 포함하는 사용자 상세 정보 클래스
 * - User 엔티티를 래핑하여 시큐리티 인증 프로세스에 사용됨
 */
@Getter
public class CustomUserDetails implements UserDetails {

    // 실제 도메인 사용자 엔티티
    private final User user;

    /**
     * 생성자
     * @param user User 엔티티 객체
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * 사용자 고유 ID 반환
     * - 인증된 사용자의 식별자(ID)를 제공
     * @return 사용자 ID(Long)
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * 사용자의 권한 정보 반환
     * - 스프링 시큐리티에서 ROLE_ 접두사를 붙인 권한 리스트로 반환
     * @return 권한 리스트
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Role enum 이름에 ROLE_ 접두사 붙여 권한 객체 생성 후 리스트로 반환
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /**
     * 사용자의 비밀번호 반환
     * - 암호화된 비밀번호를 반환하여 인증에 사용됨
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자의 로그인 아이디 반환
     * - 인증 시 사용되는 식별자
     * @return 로그인 아이디 (username)
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 계정 만료 여부 반환
     * @return 만료되지 않은 경우 true, 만료된 경우 false
     */
    @Override
    public boolean isAccountNonExpired() {
        // 만료 로직 없으면 항상 true 반환
        return true;
    }

    /**
     * 계정 잠금 여부 반환
     * @return 잠금되지 않은 경우 true, 잠긴 경우 false
     */
    @Override
    public boolean isAccountNonLocked() {
        // 잠금 로직 없으면 항상 true 반환
        return true;
    }

    /**
     * 자격 증명(비밀번호) 만료 여부 반환
     * @return 만료되지 않은 경우 true, 만료된 경우 false
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // 만료 로직 없으면 항상 true 반환
        return true;
    }

    /**
     * 사용자 활성화 여부 반환
     * - User 엔티티의 상태가 ACTIVE인 경우 true 반환
     * @return 활성화 상태면 true, 아니면 false
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }
}
