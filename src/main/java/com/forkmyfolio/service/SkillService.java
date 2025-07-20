package com.forkmyfolio.service;

import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing skills.
 * Defines business logic for creating, retrieving, and deleting skills.
 * This service operates solely on domain models (e.g., Skill) and is DTO-agnostic.
 */
public interface SkillService {

    /**
     * Retrieves the list of public skills for the portfolio owner.
     *
     * @return A list of {@link Skill} entities.
     */
    List<Skill> getPublicSkills();

    /**
     * Retrieves a single skill by its public UUID.
     *
     * @param uuid The UUID of the skill to find.
     * @return The {@link Skill} entity.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if no skill is found with the given UUID.
     */
    Skill getSkillByUuid(UUID uuid);

    /**
     * Creates and persists a new skill.
     *
     * @param skill The skill entity to save. The owner (User) must be set before calling this method.
     * @return The persisted {@link Skill} entity, including its generated ID and UUID.
     */
    Skill createSkill(Skill skill);

    /**
     * Deletes a skill by its public UUID.
     * Implementations of this method must perform an authorization check.
     *
     * @param uuid        The UUID of the skill to delete.
     * @param currentUser The user performing the action, for authorization checks.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException       if the skill is not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not authorized to delete the skill.
     */
    void deleteSkill(UUID uuid, User currentUser);

    /**
     * Updates an existing skill.
     *
     * @param uuid                The UUID of the skill to update.
     * @param updatedSkillDetails A transient Skill object with the new details.
     * @param currentUser         The user performing the action.
     * @return The updated Skill entity.
     */
    Skill updateSkill(UUID uuid, Skill updatedSkillDetails, User currentUser);
}