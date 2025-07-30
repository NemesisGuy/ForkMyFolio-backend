package com.forkmyfolio.service;

import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
     * @param email           The user's email.
     * @param password        The user's raw password.
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param profileImageUrl The URL for the user's profile image.
     * @param roles           The roles to assign to the user.
     * @param active          The active status of the user.
     * @return The created {@link User} entity.
     * @throws com.forkmyfolio.exception.EmailAlreadyExistsException if the email is already in use.
     */
    @Transactional
    User registerUser(String email, String password, String firstName, String lastName, String profileImageUrl, Set<Role> roles, Boolean active);

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return The {@link User} entity if found.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if user is not found.
     */
    User findByEmail(String email);

    @Transactional(readOnly = true)
    User getUserById(Long userId);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email The email address to check.
     * @return {@code true} if a user with the email exists, {@code false} otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return The {@link User} object representing the current user.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no user is authenticated.
     */
    User getCurrentAuthenticatedUser();

    User getPublicProfile();

    /**
     * Updates the profile information for a given user.
     *
     * @param userId          The ID of the user to update.
     * @param firstName       The new first name.
     * @param lastName        The new last name.
     * @param profileImageUrl The new profile image URL.
     * @return The updated {@link User} entity.
     */
    User updateUserProfile(Long userId, String firstName, String lastName, String profileImageUrl);

    /**
     * Retrieves a paginated list of all users. For administrative purposes.
     *
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of {@link User} entities.
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * Updates a user's details from an admin context.
     *
     * @param userId    The UUID of the user to update.
     * @param firstName The user's new first name.
     * @param lastName  The user's new last name.
     * @param slug      The user's new slug.
     * @param roles     The user's new set of roles.
     * @param active    The user's new active status.
     * @return The updated {@link User} entity.
     */
    User updateUserByAdmin(UUID userId, String firstName, String lastName, String slug, Set<Role> roles, Boolean active);

    /**
     * Deactivates a user's account (soft delete).
     *
     * @param userId The UUID of the user to deactivate.
     */
    void deactivateUser(UUID userId);

    Optional<User> findBySlug(String slug);

    User getUserByUuid(UUID userId);
}