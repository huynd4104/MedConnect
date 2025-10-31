package com.medconnect.service;

import com.medconnect.entity.User;
import com.medconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user bằng email (username)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("MSG05: Invalid credentials."));

        // 2. Kiểm tra các điều kiện custom (verified, blocked, locked)
        if (!user.getVerified()) {
            throw new CustomAuthenticationException("MSG06: Please verify your email.");
        }
        if (user.getBlocked()) {
            throw new CustomAuthenticationException("MSG07: Account is blocked.");
        }
        if (user.getLockoutEndTime() != null && user.getLockoutEndTime().isAfter(LocalDateTime.now())) {
            throw new CustomAuthenticationException("MSG08: Account locked.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}