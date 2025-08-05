package com.forkmyfolio.service;

import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for user-related business logic.
 * The implementation of this service also handles Spring Security's UserDetailsService contract.
 */
public interface UserService {

    /**
     * Registers a new user. Can be used for public registration or admin creation.
     * Default roles and active status are applied if not provided.
     *
     * @param email           The user's email.
     * @param password        The user's raw password.
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param profileImageUrl The URL for the user's profile image (can be null).
     * @param roles           The roles to assign to the user (can be null for default).
     * @param active          The active status of the user (can be null for default).
     * @return The created {@link User} entity.
     * @throws com.forkmyfolio.exception.DuplicateResourceException if the email is already in use.
     */
    User registerUser(String email, String password, String firstName, String lastName, String profileImageUrl, Set<Role> roles, Boolean active);

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return The {@link User} object representing the current user.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no user is authenticated.
     */
    User getCurrentAuthenticatedUser();

    /**
     * Updates the profile information for the currently authenticated user.
     *
     * @param firstName       The new first name.
     * @param lastName        The new last name.
     * @param profileImageUrl The new profile image URL.
     * @return The updated {@link User} entity.
     */
    User updateUserProfile(String firstName, String lastName, String profileImageUrl);

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
     * @param uuid      The UUID of the user to update.
     * @param firstName The user's new first name.
     * @param lastName  The user's new last name.
     * @param slug      The user's new slug.
     * @param roles     The user's new set of roles.
     * @param active    The user's new active status.
     * @return The updated {@link User} entity.
     */
    User updateUserByAdmin(UUID uuid, String firstName, String lastName, String slug, Set<Role> roles, Boolean active);

    /**
     * Deactivates a user's account (soft delete).
     *
     * @param uuid The UUID of the user to deactivate.
     */
    void deactivateUser(UUID uuid);

    /**
     * Finds a user by their public portfolio slug.
     *
     * @param slug The slug to search for.
     * @return An {@link Optional} containing the {@link User} if found.
     */
    Optional<User> findBySlug(String slug);

    /**
     * Finds a user by their public UUID.
     *
     * @param uuid The UUID of the user to find.
     * @return The found {@link User} entity.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if no user is found.
     */
    User getUserByUuid(UUID uuid);

    /**
     * Ensures the default administrator user exists, creating or updating it as needed
     * based on application properties. This is an idempotent operation.
     *
     * @param email     The admin's email.
     * @param password  The admin's raw password.
     * @param firstName The admin's first name.
     * @param lastName  The admin's last name.
     */
    void createOrUpdateAdminUser(String email, String password, String firstName, String lastName);
}