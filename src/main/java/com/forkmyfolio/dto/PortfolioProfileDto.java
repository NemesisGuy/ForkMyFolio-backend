package com.forkmyfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the publicly accessible portfolio profile.
 * This object aggregates all the necessary information for the main profile/about page.
 */
@Data
@NoArgsConstructor
@Schema(name = "PortfolioProfileDto", description = "Publicly available profile information for the portfolio owner.")
public class PortfolioProfileDto {

    // --- From User Entity ---
    @Schema(description = "The first name of the portfolio owner.", example = "Jane")
    private String firstName;

    @Schema(description = "The last name of the portfolio owner.", example = "Doe")
    private String lastName;

    // --- From PortfolioProfile Entity ---
    @Schema(description = "A professional headline for the profile.", example = "Full-Stack Software Engineer | Java & Vue.js Specialist")
    private String headline;

    @Schema(description = "A detailed summary or biography.", example = "A passionate developer with five years of experience...")
    private String summary;

    @Schema(description = "The main profile image URL.", example = "https://example.com/profile.png")
    @JsonProperty("profileImageUrl") // Ensure consistent naming with other DTOs
    private String imageUrl;

    @Schema(description = "The user's general location.", example = "San Francisco, CA")
    private String location;

    @Schema(description = "A public email address for contact.", example = "contact.jane@example.com")
    private String publicEmail;

    @Schema(description = "URL to a personal website or blog.", example = "https://jane-doe-portfolio.com")
    private String websiteUrl;

    @Schema(description = "URL to the user's LinkedIn profile.", example = "https://linkedin.com/in/janedoe")
    private String linkedinUrl;

    @Schema(description = "URL to the user's GitHub profile.", example = "https://github.com/janedoe")
    private String githubUrl;

    @Schema(description = "URL to a downloadable PDF version of the user's resume.", example = "https://example.com/jane-doe-resume.pdf")
    private String resumeUrl;

    @Schema(description = "A generic cover letter template that can be displayed on the portfolio.", example = "Dear Hiring Manager, I am excited to apply for...")
    private String coverLetterTemplate;
}