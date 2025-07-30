package com.forkmyfolio.service;

import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.enums.QualificationLevel;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing Qualification entities.
 * Defines the business logic for qualification-related operations,
 * ensuring that all actions are performed on behalf of the currently authenticated user.
 */
public interface QualificationService {

    /**
     * Retrieves all qualifications for the currently authenticated user.
     *
     * @return A list of {@link Qualification} entities.
     */
    List<Qualification> getQualificationsForCurrentUser();

    /**
     * Retrieves a single qualification by its UUID, ensuring it belongs to the current user.
     *
     * @param qualificationUuid The UUID of the qualification to retrieve.
     * @return The found {@link Qualification} entity.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the qualification does not exist or does not belong to the user.
     */
    Qualification getQualificationByUuidForCurrentUser(UUID qualificationUuid);

    /**
     * Creates a new qualification and associates it with the currently authenticated user.
     *
     * @param qualificationName  The name of the qualification.
     * @param institutionName    The name of the institution.
     * @param institutionLogoUrl URL for the institution's logo.
     * @param institutionWebsite URL for the institution's website.
     * @param fieldOfStudy       The field of study.
     * @param level              The academic level.
     * @param startYear          The start year.
     * @param completionYear     The completion year.
     * @param stillStudying      Flag for ongoing studies.
     * @param grade              The grade achieved.
     * @param credentialUrl      URL for a verifiable credential.
     * @param visible            Visibility on the public portfolio.
     * @return The newly created and persisted {@link Qualification} entity.
     */
    Qualification createQualificationForCurrentUser(String qualificationName, String institutionName, String institutionLogoUrl, String institutionWebsite, String fieldOfStudy, QualificationLevel level, Integer startYear, Integer completionYear, Boolean stillStudying, String grade, String credentialUrl, boolean visible);

    /**
     * Updates an existing qualification for the currently authenticated user.
     *
     * @param qualificationUuid The UUID of the qualification to update.
     * @return The updated and persisted {@link Qualification} entity.
     */
    Qualification updateQualificationForCurrentUser(UUID qualificationUuid, String qualificationName, String institutionName, String institutionLogoUrl, String institutionWebsite, String fieldOfStudy, QualificationLevel level, Integer startYear, Integer completionYear, Boolean stillStudying, String grade, String credentialUrl, Boolean visible);

    /**
     * Deletes a qualification for the currently authenticated user.
     *
     * @param qualificationUuid The UUID of the qualification to delete.
     */
    void deleteQualificationForCurrentUser(UUID qualificationUuid);
}