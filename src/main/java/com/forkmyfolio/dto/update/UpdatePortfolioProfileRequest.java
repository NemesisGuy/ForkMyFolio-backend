package com.forkmyfolio.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdatePortfolioProfileRequest {

    @Size(max = 100)
    private String headline;

    @Size(max = 5000)
    private String summary;

    @Email
    @Size(max = 255)
    private String publicEmail;

    @Size(max = 50)
    private String location;

    @URL @Size(max = 255)
    private String websiteUrl;

    @URL @Size(max = 255)
    private String linkedinUrl;

    @URL @Size(max = 255)
    private String githubUrl;

    @URL @Size(max = 255)
    private String resumeUrl;

    @URL @Size(max = 255)
    private String resumeImageUrl;


    @Size(max = 10000, message = "Cover letter template must not exceed 10000 characters.")
    private String coverLetterTemplate;


}