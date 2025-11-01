package com.medconnect.service;

import com.medconnect.dto.ResetPasswordDTO; // <-- THÊM IMPORT NÀY
import com.medconnect.entity.Token;
import com.medconnect.entity.User;
import com.medconnect.repository.TokenRepository;
import com.medconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- THÊM IMPORT NÀY

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder; // <-- THÊM DEPENDENCY NÀY

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("MSG05: Invalid email."));
        String tokenStr = UUID.randomUUID().toString();
        Token token = new Token();
        token.setUser(user);
        token.setToken(tokenStr);
        token.setTokenType(Token.TokenType.PasswordReset);
        token.setExpiryDateTime(LocalDateTime.now().plusHours(1)); // hiệu lực 1h
        tokenRepository.save(token);

        String resetLink = "http://localhost:8080/reset-password?token=" + tokenStr;
        try {
            emailService.sendPasswordResetEmail(email, resetLink);
        } catch (Exception e) {
            throw new RuntimeException("MSG10: Failed to send reset email.");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        // 1. Kiểm tra mật khẩu khớp
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp.");
        }

        // 2. Kiểm tra token
        Token token = tokenRepository.findValidByTokenAndType(dto.getToken(), Token.TokenType.PasswordReset)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn. Vui lòng thử lại."));

        // 3. Lấy user và cập nhật mật khẩu
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        // 4. Đánh dấu token đã được sử dụng
        token.setUsed(true);
        tokenRepository.save(token);
    }
}