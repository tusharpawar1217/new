package com.eligibilitygpt.core.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "user_profiles", schema = "core")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Universal fixed columns (exist across ALL government exams)
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "is_pwbd")
    @Builder.Default
    private Boolean isPwbd = false;

    @Column(name = "pwbd_type", length = 100)
    private String pwbdType;

    @Column(name = "is_ex_serviceman")
    @Builder.Default
    private Boolean isExServiceman = false;

    @Column(name = "domicile_state", length = 100)
    private String domicileState;

    @Column(name = "education_level", length = 100)
    private String educationLevel;

    @Column(name = "education_specialization")
    private String educationSpecialization;

    // DYNAMIC: Exam-specific fields stored as JSONB
    // Examples: departmental_quota, sportsperson_category, defence_background
    @Type(JsonBinaryType.class)
    @Column(name = "extra_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> extraAttributes = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum Category {
        GENERAL, OBC, SC, ST, EWS
    }
}
