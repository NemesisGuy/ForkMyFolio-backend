package com.forkmyfolio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.forkmyfolio.model.enums.QualificationLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "qualifications")
@Getter
@Setter
@NoArgsConstructor
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-qualifications")
    private User user;

    /**
     * Flag to control the visibility of this qualification on the public portfolio.
     */
    @Column(nullable = false)
    private boolean visible = true;

    @NotBlank(message = "The name of the qualification cannot be blank.")
    @Size(max = 255)
    private String qualificationName; // e.g., "Bachelor of Science in Computer Science"

    @NotBlank(message = "The institution name cannot be blank.")
    @Size(max = 255)
    private String institutionName; // e.g., "University of California, Berkeley"

    @Size(max = 512)
    @Column(name = "institution_logo_url")
    @URL(message = "Institution logo URL must be a valid URL")
    private String institutionLogoUrl; // e.g., "https://cdn.university.edu/logos/berkeley.png"

    @Size(max = 255)
    @URL(message = "Institution website must be a valid URL")
    private String institutionWebsite;

    @Size(max = 255)
    private String fieldOfStudy; // e.g., "Computer Science"

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private QualificationLevel level;

    private Integer startYear;

    private Integer completionYear; // Nullable to support ongoing studies

    @Column(nullable = false)
    private Boolean stillStudying = false;

    @Size(max = 255)
    private String grade; // e.g., "First Class Honours", "GPA: 3.8/4.0"

    @Size(max = 512)
    @URL(message = "Credential URL must be a valid URL")
    private String credentialUrl; // URL to verify degree or digital badge

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}