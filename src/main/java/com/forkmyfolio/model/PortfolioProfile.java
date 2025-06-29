package com.forkmyfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
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

    // The User this profile belongs to.
    // This creates a foreign key `user_id` in the `profiles` table.
    // The `mappedBy` in the User entity will complete the bidirectional link.
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
    private String resumeUrl; // URL to the canonical PDF resume

    @Column(length = 50)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String coverLetterTemplate;
    // Timestamps
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}