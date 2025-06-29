package com.forkmyfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdateUserAccountRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50)
    private String lastName;

    // Note: email and password are intentionally excluded.
    // Changing those would require separate, more secure endpoints.
    // --- ADD THIS FIELD ---
    @URL(message = "Profile image must be a valid URL.")
    @Size(max = 255)
    private String profileImageUrl;
}