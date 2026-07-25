package com.eligibilitygpt.core.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProfileResponse {
    
    private Long id;
    private String userEmail;
    private String fullName;
    private LocalDate dateOfBirth;
    private String category;
    private String gender;
    private String educationLevel;
    private String state;
    private String district;
    private String address;
    private String phoneNumber;
    private Boolean physicallyChallenged;
    private Boolean exServiceman;
    private Boolean isComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ProfileResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getEducationLevel() {
        return educationLevel;
    }
    
    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Boolean getPhysicallyChallenged() {
        return physicallyChallenged;
    }
    
    public void setPhysicallyChallenged(Boolean physicallyChallenged) {
        this.physicallyChallenged = physicallyChallenged;
    }
    
    public Boolean getExServiceman() {
        return exServiceman;
    }
    
    public void setExServiceman(Boolean exServiceman) {
        this.exServiceman = exServiceman;
    }
    
    public Boolean getIsComplete() {
        return isComplete;
    }
    
    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}