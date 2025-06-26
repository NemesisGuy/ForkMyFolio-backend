package com.forkmyfolio.service;

import com.forkmyfolio.dto.RegisterRequest;
import com.forkmyfolio.dto.UserDto;
import com.forkmyfolio.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Service interface for user-related operations.
 * Extends Spring Security's {@link UserDetailsService} for integration with authentication mechanisms.
 */
public interface UserService extends UserDetailsService {

    /**
     * Registers a new user based on the provided registration request.
     * This involves creating a new {@link User} entity, hashing the password,
     * and saving it to the database.
     *
     * @param registerRequest DTO containing the details for the new user.
     * @return The created {@link User} entity.
     * @throws com.forkmyfolio.exception.EmailAlreadyExistsException if the email is already in use.
     */
    User registerUser(RegisterRequest registerRequest);

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return The {@link User} entity if found, otherwise null or throws an exception.
     *         (Behavior depends on implementation, typically throws UserNotFoundException via UserDetailsService)
     */
    User findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email The email address to check.
     * @return {@code true} if a user with the email exists, {@code false} otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Converts a {@link User} entity to a {@link UserDto}.
     * This is useful for preparing user data for API responses, ensuring
     * sensitive information like passwords are not exposed.
     *
     * @param user The {@link User} entity to convert.
     * @return The corresponding {@link UserDto}.
     */
    UserDto convertToDto(User user);

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return The {@link User} object representing the current user.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no user is authenticated
     *         or the authenticated principal is not a User instance.
     */
    User getCurrentAuthenticatedUser();
}
