package com.eligibilitygpt.core.controller;

import com.eligibilitygpt.core.dto.NotificationResponse;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.repository.UserRepository;
import com.eligibilitygpt.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<NotificationResponse> uploadNotification(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws java.io.IOException {
        String userEmail = authentication.getName();
        log.info("Uploading notification for user: {}", userEmail);
        
        NotificationResponse response = notificationService.uploadNotification(userEmail, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("Fetching notifications for user: {}", userId);
        
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        log.info("Fetching notification: {}", id);
        NotificationResponse notification = notificationService.getNotification(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> getNotificationStatus(@PathVariable Long id) {
        log.info("Fetching notification status: {}", id);
        NotificationResponse notification = notificationService.getNotification(id);
        return ResponseEntity.ok(notification);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
