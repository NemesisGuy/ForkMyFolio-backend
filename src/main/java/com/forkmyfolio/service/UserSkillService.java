package com.forkmyfolio.service;

import com.forkmyfolio.dto.request.CreateUserSkillDto;
import com.forkmyfolio.dto.response.UserSkillResponseDto;
import com.forkmyfolio.dto.update.UpdateUserSkillRequest;
import com.forkmyfolio.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing user-specific skills.
 */
public interface UserSkillService {

    /**
     * Creates a new skill relationship for a user.
     *
     * @param createDto The DTO containing the new skill's data.
     * @param user      The user to whom the skill will be added.
     * @return A DTO representing the newly created skill relationship.
     */
    UserSkillResponseDto createUserSkill(CreateUserSkillDto createDto, User user);

    /**
     * Retrieves all skills for a specific user.
     *
     * @param user The user whose skills are to be retrieved.
     * @return A list of DTOs representing the user's skills.
     */
    List<UserSkillResponseDto> getAllUserSkills(User user);

    /**
     * Updates an existing user-skill relationship.
     *
     * @param userSkillId The UUID of the UserSkill relationship to update.
     * @param updateDto   The DTO containing the fields to update.
     * @param user        The currently authenticated user, for ownership verification.
     * @return A DTO representing the updated skill relationship.
     */
    UserSkillResponseDto updateUserSkill(UUID userSkillId, UpdateUserSkillRequest updateDto, User user);

    /**
     * Deletes a skill relationship for a user.
     *
     * @param userSkillId The UUID of the UserSkill relationship to delete.
     * @param user        The currently authenticated user, for ownership verification.
     */
    void deleteUserSkill(UUID userSkillId, User user);
}