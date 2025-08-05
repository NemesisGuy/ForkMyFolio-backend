package com.forkmyfolio.service;

import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.update.UpdateSkillRequest;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing user-skill relationships and the global skill pool.
 */
public interface SkillService {

    /**
     * Retrieves all skills available on the platform.
     */
    List<Skill> getAllPlatformSkills();

    /**
     * Retrieves all skill relationships for a given user.
     *
     * @param user The user whose skills to retrieve.
     * @return A list of the user's {@link UserSkill} relationships.
     */
    List<UserSkill> getAllSkillsForUser(User user);

    /**
     * Retrieves a specific skill relationship for a user by the global skill's public UUID.
     *
     * @param user      The user performing the action.
     * @param skillUuid The public UUID of the global skill.
     * @return The {@link UserSkill} relationship entity.
     */
    UserSkill getSkillForUser(User user, UUID skillUuid);

    /**
     * Adds a skill to a user's portfolio. If the skill doesn't exist globally, it's created first.
     * If the user already has the skill, an exception is thrown.
     *
     * @param request The DTO containing the skill name and user-specific details.
     * @param user    The user to whom the skill will be added.
     * @return The newly created {@link UserSkill} relationship entity.
     */
    UserSkill addSkillToUser(CreateSkillRequest request, User user);

    /**
     * Updates a user's relationship with a skill (e.g., their proficiency level or visibility).
     *
     * @param skillUuid The public UUID of the global skill to update the relationship for.
     * @param request   The DTO containing the updated user-specific details.
     * @param user      The user performing the action.
     * @return The updated {@link UserSkill} relationship entity.
     */
    UserSkill updateSkillForUser(UUID skillUuid, UpdateSkillRequest request, User user);

    /**
     * Removes a skill from a user's portfolio. This does not delete the global skill itself.
     *
     * @param skillUuid The public UUID of the global skill to remove from the user's profile.
     * @param user      The user performing the action.
     */
    void removeSkillFromUser(UUID skillUuid, User user);

    /**
     * Finds existing global skills by name or creates new ones if they don't exist.
     * This is a utility method used when associating skills with projects or experiences.
     *
     * @param skillNames A set of skill names from the frontend.
     * @return A set of persisted global {@link Skill} entities, including both found and newly created ones.
     */
    Set<Skill> findOrCreateSkills(Set<String> skillNames);
}