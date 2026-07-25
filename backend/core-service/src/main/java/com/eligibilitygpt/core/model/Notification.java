package com.eligibilitygpt.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", schema = "core")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "exam_body")
    private String examBody; // SSC, RRB, IBPS, UPSC, State PSC

    @Column(name = "notification_number", length = 100)
    private String notificationNumber;

    @Column(name = "source_filename", length = 500)
    private String sourceFilename;

    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    @Column(name = "pdf_file_size_bytes")
    private Long pdfFileSizeBytes;

    @Column(name = "total_pages")
    private Integer totalPages;

    // Processing status workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PROCESSING;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    // Important dates from notification
    @Column(name = "notification_date")
    private LocalDate notificationDate;

    @Column(name = "application_start_date")
    private LocalDate applicationStartDate;

    @Column(name = "application_end_date")
    private LocalDate applicationEndDate;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PROCESSING, READY, FAILED
    }
}
