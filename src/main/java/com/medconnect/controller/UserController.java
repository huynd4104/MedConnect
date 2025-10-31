package com.medconnect.controller;

import com.medconnect.entity.User;
import com.medconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/update-fcm-token")
    public ResponseEntity<Void> updateFcmToken(@RequestBody Map<String, String> payload, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String token = payload.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().build(); // Bad request if token is missing
        }

        String email = auth.getName(); // Lấy email người dùng đang đăng nhập
        User currentUser = userRepository.findByEmail(email).orElse(null);

        if (currentUser != null) {
            currentUser.setFcmToken(token);
            userRepository.save(currentUser);
            System.out.println("Updated FCM token for user: " + email);
            return ResponseEntity.ok().build();
        } else {
            System.err.println("User not found for FCM token update: " + email);
            return ResponseEntity.notFound().build();
        }
    }
}