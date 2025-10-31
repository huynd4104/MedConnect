package com.medconnect.service;

import com.medconnect.entity.User;
import com.medconnect.entity.User.Role;
import com.medconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    // Sử dụng @Lazy để tránh lỗi vòng lặp (circular dependency) với SecurityConfig
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Tải thông tin người dùng từ Google
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");

        // 2. Tìm hoặc tạo mới người dùng trong DB của bạn
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(attributes));

        // 3. Tạo GrantedAuthorities (roles) từ User entity của bạn
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 4. Trả về một DefaultOAuth2User mới đã chứa role của bạn
        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private User createNewUser(Map<String, Object> attributes) {
        User newUser = new User();
        newUser.setEmail((String) attributes.get("email"));

        // Vì đăng nhập bằng Google, không có mật khẩu nên tạo một mật khẩu ngẫu nhiên hoặc một giá trị place-holder
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        // Gán vai trò mặc định cho người dùng mới đăng nhập bằng Google
        newUser.setRole(Role.Patient); //  Role.Patient là vai trò mặc định

        newUser.setVerified(true); // Tự động xác thực
        newUser.setBlocked(false);

        return userRepository.save(newUser);
    }
}