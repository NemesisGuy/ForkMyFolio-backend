package com.forkmyfolio.service;

import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for business logic related to Qualifications.
 * This service operates solely on domain models and is DTO-agnostic.
 */
public interface QualificationService {

    /**
     * Retrieves the list of public qualifications for the portfolio owner.
     * @return A list of {@link Qualification} entities.
     */
    List<Qualification> getPublicQualifications();

    /**
     * Retrieves a single qualification by its public UUID.
     * @param uuid The UUID of the qualification.
     * @return The {@link Qualification} entity.
     */
    Qualification getQualificationByUuid(UUID uuid);

    /**
     * Creates and persists a new qualification.
     * @param qualification The pre-constructed qualification entity to save.
     * @return The persisted {@link Qualification} entity.
     */
    Qualification createQualification(Qualification qualification);

    /**
     * Saves an updated qualification entity.
     * @param qualification The qualification entity with updated fields to be saved.
     * @return The updated and persisted {@link Qualification} entity.
     */
    Qualification save(Qualification qualification);

    /**
     * Deletes a qualification by its public UUID.
     * @param uuid The UUID of the qualification to delete.
     * @param currentUser The user performing the action.
     * @throws AccessDeniedException if the user is not authorized.
     */
    void deleteQualification(UUID uuid, User currentUser);
}