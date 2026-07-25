package com.eligibilitygpt.core.controller;

import com.eligibilitygpt.core.dto.ApplicationResponse;
import com.eligibilitygpt.core.model.Application;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.repository.UserRepository;
import com.eligibilitygpt.core.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getUserApplications(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("Fetching applications for user: {} with status: {}", userId, status);
        
        List<ApplicationResponse> applications;
        if (status != null) {
            Application.Status statusEnum = Application.Status.valueOf(status.toUpperCase());
            applications = applicationService.getUserApplicationsByStatus(userId, statusEnum);
        } else {
            applications = applicationService.getUserApplications(userId);
        }
        
        return ResponseEntity.ok(applications);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Long notificationId = ((Number) request.get("notificationId")).longValue();
        Long postId = ((Number) request.get("postId")).longValue();
        String postName = (String) request.get("postName");
        String statusStr = (String) request.get("status");
        String notes = (String) request.get("notes");
        
        Application.Status status = statusStr != null ? 
                Application.Status.valueOf(statusStr.toUpperCase()) : Application.Status.INTERESTED;
        
        log.info("Creating application for user: {}, notification: {}, post: {}", 
                userId, notificationId, postId);
        
        ApplicationResponse application = applicationService.createApplication(
                userId, notificationId, postId, postName, status, notes);
        
        return ResponseEntity.ok(application);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String statusStr = (String) request.get("status");
        String notes = (String) request.get("notes");
        
        Application.Status status = Application.Status.valueOf(statusStr.toUpperCase());
        
        log.info("Updating application {} to status: {}", id, status);
        
        ApplicationResponse application = applicationService.updateApplicationStatus(id, status, notes);
        return ResponseEntity.ok(application);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        log.info("Deleting application: {}", id);
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
