package com.forkmyfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "Represents the user's main portfolio profile, including user details.")
public class PortfolioProfileDto {

    @Schema(description = "The user's unique URL slug.", example = "jane-doe")
    private String slug;

    @Schema(description = "The user's first name.", example = "Jane")
    private String firstName;

    @Schema(description = "The user's last name.", example = "Doe")
    private String lastName;

    @Schema(description = "A professional headline.", example = "Full-Stack Java Developer")
    private String headline;

    @Schema(description = "A summary of the user's professional background.")
    private String summary;

    @Schema(description = "The user's location.", example = "San Francisco, CA")
    private String location;

    @Schema(description = "A public-facing email address.", example = "contact.me@example.com")
    private String publicEmail;

    @Schema(description = "URL to the user's personal or professional website.")
    private String websiteUrl;

    @Schema(description = "URL to the user's LinkedIn profile.")
    private String linkedinUrl;

    @Schema(description = "URL to the user's GitHub profile.")
    private String githubUrl;

    @Schema(description = "URL to the user's downloadable resume file.")
    private String resumeUrl;

    @Schema(description = "URL to an image of the user's resume.")
    private String resumeImageUrl;

    @Schema(description = "A template for cover letters.")
    private String coverLetterTemplate;

    @Schema(description = "Master toggle for the entire portfolio's public visibility.")
    @JsonProperty("isPublic")
    private boolean isPublic;

    @Schema(description = "URL to the user's main profile image.")
    private String profileImageUrl;

    @Schema(description = "Toggle for the visibility of the profile summary section.")
    private boolean visible;

}