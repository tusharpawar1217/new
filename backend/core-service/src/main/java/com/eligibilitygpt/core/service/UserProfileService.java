package com.eligibilitygpt.core.service;

import com.eligibilitygpt.core.dto.ProfileRequest;
import com.eligibilitygpt.core.dto.ProfileResponse;
import com.eligibilitygpt.core.exception.ProfileNotFoundException;
import com.eligibilitygpt.core.exception.UserNotFoundException;
import com.eligibilitygpt.core.model.User;
import com.eligibilitygpt.core.model.UserProfile;
import com.eligibilitygpt.core.repository.UserProfileRepository;
import com.eligibilitygpt.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));

        return mapToResponse(profile);
    }

    public ProfileResponse createOrUpdateProfile(Long userId, ProfileRequest request) {
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElse(UserProfile.builder().user(user).build());

        // Update profile fields
        profile.setDateOfBirth(request.getDateOfBirth());
        
        // Convert string to enum
        if (request.getCategory() != null) {
            profile.setCategory(UserProfile.Category.valueOf(request.getCategory()));
        }
        if (request.getGender() != null) {
            profile.setGender(UserProfile.Gender.valueOf(request.getGender()));
        }
        
        profile.setEducationLevel(request.getEducationLevel());
        profile.setDomicileState(request.getState());
        
        // Map additional fields to extraAttributes
        if (request.getDistrict() != null) {
            profile.getExtraAttributes().put("district", request.getDistrict());
        }
        if (request.getAddress() != null) {
            profile.getExtraAttributes().put("address", request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            profile.getExtraAttributes().put("phoneNumber", request.getPhoneNumber());
        }
        
        profile.setIsPwbd(request.getPhysicallyChallenged());
        profile.setIsExServiceman(request.getExServiceman());

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Updated profile for user: {}", userId);
        return mapToResponse(savedProfile);
    }

    public ProfileResponse patchProfile(Long userId, ProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));

        // Only update non-null fields (PATCH behavior)
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getCategory() != null) {
            profile.setCategory(UserProfile.Category.valueOf(request.getCategory()));
        }
        if (request.getGender() != null) {
            profile.setGender(UserProfile.Gender.valueOf(request.getGender()));
        }
        if (request.getEducationLevel() != null) {
            profile.setEducationLevel(request.getEducationLevel());
        }
        if (request.getState() != null) {
            profile.setDomicileState(request.getState());
        }
        if (request.getDistrict() != null) {
            profile.getExtraAttributes().put("district", request.getDistrict());
        }
        if (request.getAddress() != null) {
            profile.getExtraAttributes().put("address", request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            profile.getExtraAttributes().put("phoneNumber", request.getPhoneNumber());
        }
        if (request.getPhysicallyChallenged() != null) {
            profile.setIsPwbd(request.getPhysicallyChallenged());
        }
        if (request.getExServiceman() != null) {
            profile.setIsExServiceman(request.getExServiceman());
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Patched profile for user: {}", userId);
        return mapToResponse(savedProfile);
    }

    public boolean hasProfile(Long userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    private boolean isProfileComplete(UserProfile profile) {
        return profile.getDateOfBirth() != null &&
               profile.getCategory() != null &&
               profile.getGender() != null &&
               profile.getEducationLevel() != null;
    }

    private ProfileResponse mapToResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setUserEmail(profile.getUser().getEmail());
        response.setFullName(profile.getUser().getFullName());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setCategory(profile.getCategory() != null ? profile.getCategory().toString() : null);
        response.setGender(profile.getGender() != null ? profile.getGender().toString() : null);
        response.setEducationLevel(profile.getEducationLevel());
        response.setState(profile.getDomicileState());
        
        // Extract from extraAttributes
        response.setDistrict((String) profile.getExtraAttributes().get("district"));
        response.setAddress((String) profile.getExtraAttributes().get("address"));
        response.setPhoneNumber((String) profile.getExtraAttributes().get("phoneNumber"));
        
        response.setPhysicallyChallenged(profile.getIsPwbd());
        response.setExServiceman(profile.getIsExServiceman());
        response.setIsComplete(isProfileComplete(profile));
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}