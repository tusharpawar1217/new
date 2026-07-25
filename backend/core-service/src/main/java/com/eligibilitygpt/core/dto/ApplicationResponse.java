package com.eligibilitygpt.core.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationResponse {
    
    private Long id;
    private String userEmail;
    private String postCode;
    private String postTitle;
    private String department;
    private String status; // INTERESTED, ELIGIBLE_CONFIRMED, APPLIED, REJECTED
    private LocalDate applicationDeadline;
    private String eligibilityStatus; // ELIGIBLE, NOT_ELIGIBLE, PENDING_CHECK
    private String eligibilityReason;
    private Double eligibilityScore; // 0.0 to 1.0
    private String notesFromUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ApplicationResponse() {}
    
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
    
    public String getPostCode() {
        return postCode;
    }
    
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
    
    public String getPostTitle() {
        return postTitle;
    }
    
    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }
    
    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    
    public String getEligibilityStatus() {
        return eligibilityStatus;
    }
    
    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }
    
    public String getEligibilityReason() {
        return eligibilityReason;
    }
    
    public void setEligibilityReason(String eligibilityReason) {
        this.eligibilityReason = eligibilityReason;
    }
    
    public Double getEligibilityScore() {
        return eligibilityScore;
    }
    
    public void setEligibilityScore(Double eligibilityScore) {
        this.eligibilityScore = eligibilityScore;
    }
    
    public String getNotesFromUser() {
        return notesFromUser;
    }
    
    public void setNotesFromUser(String notesFromUser) {
        this.notesFromUser = notesFromUser;
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