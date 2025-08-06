package com.forkmyfolio.service;

import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing user-specific skills.
 * This service operates exclusively on domain entities.
 */
public interface UserSkillService {

    /**
     * Adds a new skill to a user's portfolio.
     *
     * @param user             The user to whom the skill will be added.
     * @param skillDetails     A transient entity with the global skill's data.
     * @param userSkillDetails A transient entity with the user-specific data.
     * @return The newly created and persisted {@link UserSkill} entity.
     */
    UserSkill addSkillToUser(User user, Skill skillDetails, UserSkill userSkillDetails);

    /**
     * Retrieves all skills for a specific user.
     *
     * @param user The user whose skills are to be retrieved.
     * @return A list of {@link UserSkill} entities.
     */
    List<UserSkill> getAllSkillsForUser(User user);

    /**
     * Retrieves a specific user-skill relationship.
     *
     * @param user          The user for ownership verification.
     * @param userSkillUuid The UUID of the UserSkill relationship to retrieve.
     * @return The found {@link UserSkill} entity.
     */
    UserSkill getSkillForUser(User user, UUID userSkillUuid);

    /**
     * Updates an existing user-skill relationship.
     *
     * @param userSkillUuid    The UUID of the UserSkill relationship to update.
     * @param userSkillUpdates A transient entity containing the fields to update.
     * @param user             The currently authenticated user, for ownership verification.
     * @return The updated {@link UserSkill} entity.
     */
    UserSkill updateSkillForUser(UUID userSkillUuid, UserSkill userSkillUpdates, User user);

    /**
     * Deletes a skill relationship for a user.
     *
     * @param userSkillUuid The UUID of the UserSkill relationship to delete.
     * @param user        The currently authenticated user, for ownership verification.
     */
    void removeSkillFromUser(UUID userSkillUuid, User user);
}