package com.medconnect.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.medconnect.entity.Notification;
import com.medconnect.entity.User;
import com.medconnect.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/firebase-service-account.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase initialized successfully!");
        }
    }


    public void sendPushNotification(User user, String title, String body) {
        // Giả sử user có deviceToken; ở đây demo gửi FCM
        Message message = Message.builder()
                .putData("title", title)
                .putData("body", body)
                .setToken("user_device_token") // Thay bằng token thực từ user
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            // Fallback to email
            try {
                emailService.sendFallbackNotification(user.getEmail(), title, body);
            } catch (Exception ex) {
                // Log error
            }
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(body);
        notification.setNotificationType(title);
        notificationRepository.save(notification);
    }
}