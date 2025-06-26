package com.forkmyfolio.service;

import com.forkmyfolio.dto.CreateSkillRequest;
import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;

import java.util.List;

/**
 * Service interface for managing skills.
 * Defines operations such as creating, retrieving, and deleting skills.
 */
public interface SkillService {

    /**
     * Retrieves all skills, potentially across all users or for a specific context
     * depending on implementation (for now, let's assume all skills in the system).
     *
     * @return A list of {@link SkillDto} objects.
     */
    List<SkillDto> getAllSkills();

    /**
     * Retrieves all skills for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of {@link SkillDto} objects for the specified user.
     */
    List<SkillDto> getAllSkillsByUserId(Long userId);


    /**
     * Retrieves a skill by its ID.
     *
     * @param id The ID of the skill to retrieve.
     * @return The {@link SkillDto} if found.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the skill with the given ID is not found.
     */
    SkillDto getSkillById(Long id);

    /**
     * Retrieves a skill entity by its ID.
     *
     * @param id The ID of the skill to retrieve.
     * @return The {@link Skill} entity if found.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the skill with the given ID is not found.
     */
    Skill findSkillEntityById(Long id);

    /**
     * Creates a new skill and associates it with the provided user.
     *
     * @param createSkillRequest DTO containing the details for the new skill.
     * @param currentUser The user to whom the skill will be associated.
     * @return The created {@link SkillDto}.
     */
    SkillDto createSkill(CreateSkillRequest createSkillRequest, User currentUser);

    /**
     * Deletes a skill by its ID.
     * Requires authorization to ensure the user (e.g., an admin or the skill owner)
     * has permission to delete it.
     *
     * @param id The ID of the skill to delete.
     * @param currentUser The user attempting to delete the skill.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the skill with the given ID is not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not authorized to delete the skill.
     */
    void deleteSkill(Long id, User currentUser);

    /**
     * Converts a {@link Skill} entity to a {@link SkillDto}.
     * @param skill The skill entity.
     * @return The corresponding DTO.
     */
    SkillDto convertToDto(Skill skill);

    /**
     * Converts a {@link CreateSkillRequest} DTO to a {@link Skill} entity.
     * @param request The DTO.
     * @param owner The user who will own the skill.
     * @return The skill entity.
     */
    Skill convertCreateRequestToEntity(CreateSkillRequest request, User owner);
}
