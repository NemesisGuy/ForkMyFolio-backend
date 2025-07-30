package com.forkmyfolio.service;

import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing work experiences.
 * Defines business logic for creating, retrieving, updating, and deleting experiences.
 * This service operates solely on domain models (e.g., Experience) and is DTO-agnostic.
 */
public interface ExperienceService {

    /**
     * Retrieves all experiences belonging to a specific user, ordered by displayOrder.
     *
     * @param user The user whose experiences are to be retrieved.
     * @return A list of {@link Experience} entities for the specified user.
     */
    List<Experience> getExperiencesForUser(User user);

    /**
     * Retrieves a single experience by its public UUID, ensuring it belongs to the specified user.
     *
     * @param uuid The UUID of the experience.
     * @param user The user who must own the experience.
     * @return The {@link Experience} entity if found and owned by the user.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the experience is not found.
     * @throws AccessDeniedException if the user does not own the experience.
     */
    Experience findExperienceByUuidAndUser(UUID uuid, User user);

    /**
     * Creates and persists a new experience.
     *
     * @param experience The experience entity to save. The owner (User) must be set before calling this method.
     * @param skillUuids A set of UUIDs for skills to associate with this experience.
     * @return The persisted {@link Experience} entity, including its generated ID and UUID.
     */
    Experience createExperience(Experience experience, Set<UUID> skillUuids);

    /**
     * Updates an existing experience.
     *
     * @param uuid The UUID of the experience to update.
     * @param updatedExperience An Experience object containing the new data to be applied.
     * @param skillUuids A set of UUIDs for skills to associate with this experience.
     * @param currentUser The user performing the action, for authorization.
     * @return The updated Experience entity.
     */
    Experience updateExperience(UUID uuid, Experience updatedExperience, Set<UUID> skillUuids, User currentUser);

    /**
     * Deletes an experience by its public UUID.
     * Implementations of this method must perform an authorization check.
     *
     * @param uuid        The UUID of the experience to delete.
     * @param currentUser The user performing the action, for authorization checks.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the experience is not found.
     * @throws AccessDeniedException if the user is not authorized to delete the experience.
     */
    void deleteExperience(UUID uuid, User currentUser);
}