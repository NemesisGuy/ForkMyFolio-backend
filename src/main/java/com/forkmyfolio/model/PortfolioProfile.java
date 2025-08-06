package com.forkmyfolio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Entity
@Table(name = "portfolio_profiles")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-profile") // FIX: Added to break the serialization loop.

    private User user;

    /**
     * Section-level Toggle: Controls the visibility of the profile summary section
     * on an otherwise public portfolio page.
     */
    @Column(nullable = false)
    private boolean visible = true;

    /**
     * Master-level Toggle: Controls the visibility of the entire public-facing portfolio.
     * If false, the public slug (e.g., /portfolio/john-doe) will be inaccessible.
     * Defaults to false for security.
     */
    @Column(nullable = false)
    private boolean isPublic = false;

    @Column(length = 100)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Email
    private String publicEmail;

    @URL
    private String websiteUrl;

    @URL
    private String linkedinUrl;

    @URL
    private String githubUrl;

    @URL
    private String resumeUrl; // URL to the downloadable PDF resume

    @URL
    private String resumeImageUrl; // URL to a preview image of the resume (e.g., a PNG/JPG)

    @Column(length = 50)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String coverLetterTemplate;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}