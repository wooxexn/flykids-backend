package com.mtvs.flykidsbackend.config.security;

import com.mtvs.flykidsbackend.domain.user.entity.User;
import com.mtvs.flykidsbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * username(로그인 아이디)로 사용자 조회 후 UserDetails 반환
     * @param username 로그인 아이디
     * @return UserDetails (CustomUserDetails)
     * @throws UsernameNotFoundException 해당 사용자가 없으면 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return new CustomUserDetails(user);
    }
}
