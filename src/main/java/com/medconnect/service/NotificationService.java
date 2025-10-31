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

    public void sendPushNotification(User user, String title, String body) { // Remove link parameter here
        String deviceToken = user.getFcmToken();

        if (deviceToken != null && !deviceToken.isEmpty()) {
            // Save to DB first to get the ID
            Notification savedNotification = saveNotificationToDb(user, title, body); // Pass only needed params

            Message.Builder messageBuilder = Message.builder()
                    .putData("title", title)
                    .putData("body", body)
                    .putData("notificationId", String.valueOf(savedNotification.getNotificationId())) // Send ID
                    .putData("sentAt", savedNotification.getSentAt().toString()) // Send time
                    .setToken(deviceToken);

            Message message = messageBuilder.build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Successfully sent FCM message: " + response + " to user: " + user.getEmail());
            } catch (FirebaseMessagingException e) {
                System.err.println("Failed to send FCM message to user: " + user.getEmail() + ", Error: " + e.getMessage());
                sendEmailFallback(user, title, body);
            }
        } else {
            System.out.println("No FCM token for user: " + user.getEmail() + ". Sending email fallback.");
            sendEmailFallback(user, title, body);
            // Still save to DB even if only email is sent
            saveNotificationToDb(user, title, body);
        }
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

    private Notification saveNotificationToDb(User user, String title, String body) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(body);
        notification.setNotificationType(title);
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());
        // Removed: notification.setLink(link);
        return notificationRepository.save(notification); // Return the saved entity to get the ID
    }
}