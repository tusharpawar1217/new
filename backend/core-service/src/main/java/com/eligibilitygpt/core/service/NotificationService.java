package com.eligibilitygpt.core.service;

import com.eligibilitygpt.core.dto.NotificationResponse;
import com.eligibilitygpt.core.exception.NotificationNotFoundException;
import com.eligibilitygpt.core.exception.UserNotFoundException;
import com.eligibilitygpt.core.model.Notification;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.repository.NotificationRepository;
import com.eligibilitygpt.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/notifications";

    public NotificationResponse uploadNotification(String userEmail, MultipartFile file) throws IOException {
        // Verify user exists
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String filePath = UPLOAD_DIR + "/" + uniqueFileName;

        // Save file to disk
        Path targetPath = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Create notification record
        Notification notification = Notification.builder()
                .title(originalFileName) // Use original filename as title for now
                .sourceFilename(originalFileName)
                .pdfFilePath(filePath)
                .pdfFileSizeBytes(file.getSize())
                .uploadedBy(user)
                .status(Notification.Status.PROCESSING)
                .processingStartedAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification uploaded: {} by user: {}", savedNotification.getId(), userEmail);

        // TODO: Trigger async processing by calling RAG service
        // This would be done via message queue or direct HTTP call
        
        return mapToResponse(savedNotification);
    }

    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Getting notifications for user: {}", userId);
        Page<Notification> notifications = notificationRepository.findByUploadedById(userId, pageable);
        return notifications.map(this::mapToResponse);
    }

    public NotificationResponse getNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));
        return mapToResponse(notification);
    }

    public void updateNotificationStatus(Long notificationId, Notification.Status status, String error) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));

        notification.setStatus(status);
        if (error != null) {
            notification.setProcessingError(error);
        }
        
        if (status == Notification.Status.READY || status == Notification.Status.FAILED) {
            notification.setProcessingCompletedAt(LocalDateTime.now());
        }

        notificationRepository.save(notification);
        log.info("Updated notification {} status to: {}", notificationId, status);
    }

    public void updateNotificationMetadata(Long notificationId, Integer totalPages, String examBody) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));

        if (totalPages != null) {
            notification.setTotalPages(totalPages);
        }
        if (examBody != null) {
            notification.setExamBody(examBody);
        }
        
        notificationRepository.save(notification);
        log.info("Updated notification {} metadata", notificationId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setFileName(notification.getSourceFilename());
        response.setOriginalFileName(notification.getSourceFilename());
        response.setFilePath(notification.getPdfFilePath());
        response.setFileSizeBytes(notification.getPdfFileSizeBytes());
        response.setStatus(notification.getStatus().toString());
        response.setTotalPages(notification.getTotalPages());
        response.setTotalPosts(null); // Will be added when post extraction is implemented
        response.setProcessingMessage(notification.getProcessingError());
        response.setProcessingProgress(calculateProgress(notification.getStatus()));
        response.setUploadedAt(notification.getUploadedAt());
        response.setCompletedAt(notification.getProcessingCompletedAt());
        response.setUserEmail(notification.getUploadedBy().getEmail());
        return response;
    }

    private Double calculateProgress(Notification.Status status) {
        return switch (status) {
            case PROCESSING -> 0.5;
            case READY -> 1.0;
            case FAILED -> 0.0;
        };
    }
}