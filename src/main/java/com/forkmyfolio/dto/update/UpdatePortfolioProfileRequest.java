package com.forkmyfolio.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.Optional;

@Data
public class UpdatePortfolioProfileRequest {

    private Optional<@Size(max = 100) String> headline = Optional.empty();

    private Optional<@Size(max = 5000) String> summary = Optional.empty();

    private Optional<@Email @Size(max = 255) String> publicEmail = Optional.empty();

    private Optional<@Size(max = 50) String> location = Optional.empty();

    private Optional<@URL @Size(max = 255) String> websiteUrl = Optional.empty();

    private Optional<@URL @Size(max = 255) String> linkedinUrl = Optional.empty();

    private Optional<@URL @Size(max = 255) String> githubUrl = Optional.empty();

    private Optional<@URL @Size(max = 255) String> resumeUrl = Optional.empty();

    private Optional<@URL @Size(max = 255) String> resumeImageUrl = Optional.empty();

    private Optional<@Size(max = 10000, message = "Cover letter template must not exceed 10000 characters.") String> coverLetterTemplate = Optional.empty();
}
