package com.forkmyfolio.service;

import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing skills.
 * Defines business logic for creating, retrieving, and deleting skills.
 * This service operates solely on domain models (e.g., Skill) and is DTO-agnostic.
 */
public interface SkillService {

    /**
     * Retrieves all skills belonging to a specific user.
     *
     * @param user The user whose skills are to be retrieved.
     * @return A list of {@link Skill} entities for the specified user.
     */
    List<Skill> getSkillsForUser(User user);

    /**
     * Retrieves a single skill by its public UUID, without an ownership check.
     *
     * @param uuid The UUID of the skill to find.
     * @return The {@link Skill} entity.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if no skill is found with the given UUID.
     */
    Skill findSkillByUuid(UUID uuid);

    /**
     * Retrieves a single skill by its UUID, ensuring it belongs to the specified user.
     *
     * @param uuid The UUID of the skill.
     * @param user The user who must own the skill.
     * @return The {@link Skill} entity if found and owned by the user.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the skill is not found.
     * @throws AccessDeniedException if the user does not own the skill.
     */
    Skill findSkillByUuidAndUser(UUID uuid, User user);

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
     * @throws AccessDeniedException if the user is not authorized to delete the skill.
     */
    void deleteSkill(UUID uuid, User currentUser);

    /**
     * Updates an existing skill.
     *
     * @param uuid        The UUID of the skill to update.
     * @param name        The new name for the skill.
     * @param level       The new proficiency level for the skill.
     * @param visible     The new visibility status for the skill.
     * @param category    The new category for the skill.
     * @param icon        The new icon for the skill.
     * @param description The new description for the skill.
     * @param currentUser The user performing the action.
     * @return The updated Skill entity.
     */
    Skill updateSkill(UUID uuid, String name, Skill.SkillLevel level, boolean visible, String category, String icon, String description, User currentUser);
}