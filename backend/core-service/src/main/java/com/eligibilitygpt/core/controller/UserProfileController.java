package com.eligibilitygpt.core.controller;

import com.eligibilitygpt.core.dto.ProfileRequest;
import com.eligibilitygpt.core.dto.ProfileResponse;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.repository.UserRepository;
import com.eligibilitygpt.core.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("Fetching profile for user: {}", userId);
        
        try {
            ProfileResponse profile = profileService.getProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            // Profile doesn't exist yet
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestBody ProfileRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("Updating profile for user: {}", userId);
        
        ProfileResponse profile = profileService.createOrUpdateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> patchProfile(
            @RequestBody ProfileRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("Patching profile for user: {}", userId);
        
        ProfileResponse profile = profileService.patchProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
