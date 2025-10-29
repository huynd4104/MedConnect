package com.medconnect.controller;

import com.medconnect.entity.Notification;
import com.medconnect.entity.User;
import com.medconnect.repository.NotificationRepository;
import com.medconnect.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách thông báo chưa đọc của người dùng hiện tại.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        List<Notification> unreadNotifications = notificationRepository.findByUserUserIdAndReadFalseOrderBySentAtDesc(currentUser.getUserId());
        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * Đánh dấu một thông báo cụ thể là đã đọc.
     */
    @PostMapping("/mark-read/{id}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable("id") Integer notificationId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        // Kiểm tra xem thông báo này có thuộc về người dùng đang đăng nhập không
        if (!notification.getUser().getUserId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    /**
     * Đánh dấu tất cả thông báo chưa đọc của người dùng hiện tại là đã đọc.
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        List<Notification> unreadNotifications = notificationRepository.findByUserUserIdAndReadFalseOrderBySentAtDesc(currentUser.getUserId());
        if (!unreadNotifications.isEmpty()) {
            for (Notification notification : unreadNotifications) {
                notification.setRead(true);
            }
            notificationRepository.saveAll(unreadNotifications);
        }
        return ResponseEntity.ok().build();
    }
}