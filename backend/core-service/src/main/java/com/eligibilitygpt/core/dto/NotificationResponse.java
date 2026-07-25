package com.eligibilitygpt.core.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    
    private Long id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSizeBytes;
    private String status; // UPLOADED, PROCESSING, COMPLETED, FAILED
    private Integer totalPages;
    private Integer totalPosts;
    private String processingMessage;
    private Double processingProgress; // 0.0 to 1.0
    private LocalDateTime uploadedAt;
    private LocalDateTime completedAt;
    private String userEmail;
    
    public NotificationResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Integer getTotalPosts() {
        return totalPosts;
    }
    
    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }
    
    public String getProcessingMessage() {
        return processingMessage;
    }
    
    public void setProcessingMessage(String processingMessage) {
        this.processingMessage = processingMessage;
    }
    
    public Double getProcessingProgress() {
        return processingProgress;
    }
    
    public void setProcessingProgress(Double processingProgress) {
        this.processingProgress = processingProgress;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}