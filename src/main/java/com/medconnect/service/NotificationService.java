package com.medconnect.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
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
import java.time.LocalDateTime;

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
        // Lấy token thật từ User entity
        String deviceToken = user.getFcmToken();

        // Chỉ gửi FCM nếu có token
        if (deviceToken != null && !deviceToken.isEmpty()) {
            Message message = Message.builder()
                    .putData("title", title) // Dùng data payload để tùy chỉnh thông báo ở client
                    .putData("body", body)
                    // .setNotification(com.google.firebase.messaging.Notification.builder() // Có thể dùng Notification payload nếu muốn thông báo hệ thống đơn giản
                    //         .setTitle(title)
                    //         .setBody(body)
                    //         .build())
                    .setToken(deviceToken) // <-- SỬ DỤNG TOKEN THẬT
                    .build();
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Successfully sent FCM message: " + response + " to user: " + user.getEmail());
            } catch (FirebaseMessagingException e) {
                System.err.println("Failed to send FCM message to user: " + user.getEmail() + ", Error: " + e.getMessage());
                // Fallback to email only if FCM fails
                sendEmailFallback(user, title, body);
            }
        } else {
            // Nếu không có token, chỉ gửi email
            System.out.println("No FCM token for user: " + user.getEmail() + ". Sending email fallback.");
            sendEmailFallback(user, title, body);
        }

        // Lưu vào DB (giữ nguyên)
        saveNotificationToDb(user, title, body);
    }

    // Tách logic gửi mail và lưu DB ra hàm riêng cho rõ ràng
    private void sendEmailFallback(User user, String title, String body) {
        try {
            emailService.sendFallbackNotification(user.getEmail(), title, body);
        } catch (Exception ex) {
            System.err.println("Failed to send fallback email to user: " + user.getEmail() + ", Error: " + ex.getMessage());
            // Log error nghiêm trọng hơn nếu cần
        }
    }

    private void saveNotificationToDb(User user, String title, String body) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(body);
        // Lưu title vào notificationType có vẻ hợp lý hơn
        notification.setNotificationType(title);
        notification.setRead(false); // Đảm bảo mặc định là chưa đọc
        notification.setSentAt(LocalDateTime.now()); // Lấy thời gian hiện tại chính xác hơn
        notificationRepository.save(notification);
    }
}