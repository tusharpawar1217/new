package com.eligibilitygpt.core.service;

import com.eligibilitygpt.core.dto.ApplicationResponse;
import com.eligibilitygpt.core.exception.ApplicationNotFoundException;
import com.eligibilitygpt.core.exception.UserNotFoundException;
import com.eligibilitygpt.core.model.Application;
import com.eligibilitygpt.core.model.Notification;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.repository.ApplicationRepository;
import com.eligibilitygpt.core.repository.NotificationRepository;
import com.eligibilitygpt.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public List<ApplicationResponse> getUserApplications(Long userId) {
        log.debug("Getting all applications for user: {}", userId);
        List<Application> applications = applicationRepository.findByUserId(userId);
        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getUserApplicationsByStatus(Long userId, Application.Status status) {
        log.debug("Getting applications for user: {} with status: {}", userId, status);
        List<Application> applications = applicationRepository.findByUserIdAndStatus(userId, status);
        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse createApplication(Long userId, Long notificationId, Long postId, 
                                               String postName, Application.Status status, String notes) {
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Verify notification exists
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        // Check if application already exists
        if (applicationRepository.existsByUserIdAndNotificationIdAndPostId(userId, notificationId, postId)) {
            throw new IllegalArgumentException("Application already exists for this post");
        }

        Application application = Application.builder()
                .user(user)
                .notification(notification)
                .postId(postId)
                .status(status)
                .notes(notes)
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Created application: {} for user: {} and post: {}", savedApplication.getId(), userId, postId);
        
        return mapToResponse(savedApplication);
    }

    public ApplicationResponse updateApplicationStatus(Long applicationId, Application.Status status, String notes) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));

        application.setStatus(status);
        if (notes != null) {
            application.setNotes(notes);
        }

        Application savedApplication = applicationRepository.save(application);
        log.info("Updated application {} status to: {}", applicationId, status);
        
        return mapToResponse(savedApplication);
    }

    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));

        applicationRepository.delete(application);
        log.info("Deleted application: {}", applicationId);
    }

    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setUserEmail(application.getUser().getEmail());
        response.setPostCode(application.getPostId().toString()); // Using postId as code for now
        response.setPostTitle("Post " + application.getPostId()); // Will be enhanced when post data is available
        response.setDepartment(application.getNotification().getExamBody());
        response.setStatus(application.getStatus().toString());
        response.setApplicationDeadline(application.getNotification().getApplicationEndDate());
        response.setEligibilityStatus("PENDING_CHECK"); // Will be set by eligibility service
        response.setEligibilityReason(null);
        response.setEligibilityScore(null);
        response.setNotesFromUser(application.getNotes());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}