package com.forkmyfolio.service;

import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for business logic related to Work Experience.
 * This service operates solely on domain models and is DTO-agnostic.
 */
public interface ExperienceService {

    /**
     * Retrieves the list of public work experiences for the portfolio owner.
     *
     * @return A list of {@link Experience} entities.
     */
    List<Experience> getPublicExperience();

    /**
     * Retrieves a single experience by its public UUID.
     *
     * @param uuid The UUID of the experience.
     * @return The {@link Experience} entity.
     */
    Experience getExperienceByUuid(UUID uuid);

    /**
     * Creates and persists a new experience.
     *
     * @param experience The pre-constructed experience entity to save.
     * @return The persisted {@link Experience} entity.
     */
    Experience createExperience(Experience experience);

    /**
     * Saves an updated experience entity.
     *
     * @param experience The experience entity with updated fields to be saved.
     * @return The updated and persisted {@link Experience} entity.
     */
    Experience save(Experience experience);

    /**
     * Deletes an experience by its public UUID.
     *
     * @param uuid        The UUID of the experience to delete.
     * @param currentUser The user performing the action.
     * @throws AccessDeniedException if the user is not authorized.
     */
    void deleteExperience(UUID uuid, User currentUser);
}