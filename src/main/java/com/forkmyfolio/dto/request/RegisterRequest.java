package com.forkmyfolio.dto.request;

import com.forkmyfolio.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Data Transfer Object for new user registration requests.
 * Contains the information needed to create a new user account.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * User's email address. Must be unique and a valid email format.
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * User's first name.
     */
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /**
     * User's desired password.
     * Must be at least 8 characters long.
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    /**
     * URL to the user's profile image (optional).
     */
    private String profileImageUrl;

    /**
     * Roles to be assigned to the user upon registration.
     * If not provided, a default role (e.g., USER) might be assigned by the backend.
     * For this project, we'll allow specifying roles, but typically this might be restricted.
     */
    private Set<Role> roles; // Example: ["USER"], ["ADMIN", "USER"]
}
