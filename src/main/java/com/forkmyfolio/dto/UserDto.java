package com.forkmyfolio.dto;

import com.forkmyfolio.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for representing User information in API responses.
 * Excludes sensitive information like the password.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Unique identifier for the user.
     */
    private UUID id;

    /**
     * User's email address.
     */
    private String email;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * URL to the user's profile image.
     */
    private String profileImageUrl;

    /**
     * Roles assigned to the user (e.g., ADMIN, USER).
     */
    private Set<Role> roles;

    /**
     * Timestamp of when the user account was created.
     */
    private LocalDateTime createdAt;
}
