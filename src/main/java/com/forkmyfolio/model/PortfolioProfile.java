package com.forkmyfolio.model;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    // --- ADD THIS NEW FIELD ---
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