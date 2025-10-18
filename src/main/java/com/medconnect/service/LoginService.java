package com.medconnect.service;

import com.medconnect.entity.Token;
import com.medconnect.entity.User;
import com.medconnect.repository.TokenRepository;
import com.medconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("MSG05: Invalid email."));
        String tokenStr = UUID.randomUUID().toString();
        Token token = new Token();
        token.setUser(user);
        token.setToken(tokenStr);
        token.setTokenType(Token.TokenType.PasswordReset);
        token.setExpiryDateTime(LocalDateTime.now().plusHours(1));
        tokenRepository.save(token);

        String resetLink = "http://localhost:8080/reset-password?token=" + tokenStr;
        try {
            emailService.sendPasswordResetEmail(email, resetLink);
        } catch (Exception e) {
            throw new RuntimeException("MSG10: Failed to send reset email.");
        }
    }
}