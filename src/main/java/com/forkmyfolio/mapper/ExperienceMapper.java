package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateExperienceRequest;
import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.dto.update.UpdateExperienceRequest;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting Experience entities to DTOs and vice-versa.
 */
@Component
@RequiredArgsConstructor
public class ExperienceMapper {

    private final SkillMapper skillMapper;

    /**
     * Converts an Experience entity to its DTO representation.
     * This version provides basic skill information without user-specific details.
     *
     * @param experience The Experience entity.
     * @return The corresponding ExperienceDto.
     */
    public ExperienceDto toDto(Experience experience) {
        if (experience == null) {
            return null;
        }

        ExperienceDto dto = new ExperienceDto();
        dto.setUuid(experience.getUuid());
        dto.setJobTitle(experience.getJobTitle());
        dto.setCompanyName(experience.getCompanyName());
        dto.setCompanyUrl(experience.getCompanyUrl());
        dto.setCompanyLogoUrl(experience.getCompanyLogoUrl());
        dto.setLocation(experience.getLocation());
        dto.setLocationType(experience.getLocationType());
        dto.setEmploymentType(experience.getEmploymentType());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setDescription(experience.getDescription());
        dto.setAchievements(experience.getAchievements());
        dto.setVisible(experience.isVisible());
        dto.setDisplayOrder(experience.getDisplayOrder());
        dto.setCreatedAt(experience.getCreatedAt());
        dto.setUpdatedAt(experience.getUpdatedAt());

        if (experience.getSkills() != null) {
            dto.setSkills(experience.getSkills().stream()
                    .map(skillMapper::toDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setSkills(Collections.emptySet());
        }

        return dto;
    }

    /**
     * Converts an Experience entity to a DTO, enriching it with user-specific skill data.
     * This is the preferred method for public portfolio views where detailed skill context is needed.
     *
     * @param experience  The Experience entity to convert.
     * @param skillLookup A map where the key is the global Skill UUID and the value is the user-specific UserSkill entity.
     * @return A detailed ExperienceDto with user-specific skill information.
     */
    public ExperienceDto toDto(Experience experience, Map<UUID, UserSkill> skillLookup) {
        if (experience == null) {
            return null;
        }

        ExperienceDto dto = toDto(experience); // Start with the basic mapping

        // Now, enrich the skills using the lookup map
        if (experience.getSkills() != null) {
            dto.setSkills(experience.getSkills().stream()
                    .map(skill -> {
                        UserSkill userSkill = skillLookup.get(skill.getUuid());
                        if (userSkill != null) {
                            // If found, use the detailed mapper to include level, description, etc.
                            return skillMapper.toDetailDto(userSkill);
                        } else {
                            // Fallback to the basic mapper if no user-specific data is available
                            return skillMapper.toDto(skill);
                        }
                    })
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    /**
     * Converts a CreateExperienceRequest DTO to a new, transient Experience entity.
     *
     * @param request The DTO with creation data.
     * @return A new Experience entity.
     */
    public Experience toEntity(CreateExperienceRequest request) {
        if (request == null) {
            return null;
        }
        Experience experience = new Experience();
        experience.setJobTitle(request.getJobTitle());
        experience.setCompanyName(request.getCompanyName());
        experience.setCompanyUrl(request.getCompanyUrl());
        experience.setCompanyLogoUrl(request.getCompanyLogoUrl());
        experience.setLocation(request.getLocation());
        experience.setLocationType(request.getLocationType());
        experience.setEmploymentType(request.getEmploymentType());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        experience.setAchievements(request.getAchievements());
        experience.setVisible(request.isVisible());
        experience.setDisplayOrder(request.getDisplayOrder());
        return experience;
    }

    /**
     * Converts an ExperienceDto (from a backup) to a new Experience entity.
     *
     * @param dto  The DTO with experience data.
     * @param user The user who will own this experience.
     * @return A new Experience entity.
     */
    public Experience toEntityFromDto(ExperienceDto dto, User user) {
        if (dto == null) {
            return null;
        }
        Experience experience = new Experience();
        experience.setUuid(dto.getUuid());
        experience.setUser(user);
        experience.setJobTitle(dto.getJobTitle());
        experience.setCompanyName(dto.getCompanyName());
        experience.setCompanyUrl(dto.getCompanyUrl());
        experience.setCompanyLogoUrl(dto.getCompanyLogoUrl());
        experience.setLocation(dto.getLocation());
        experience.setLocationType(dto.getLocationType());
        experience.setEmploymentType(dto.getEmploymentType());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setDescription(dto.getDescription());
        experience.setAchievements(dto.getAchievements());
        experience.setVisible(dto.isVisible());
        experience.setDisplayOrder(dto.getDisplayOrder());
        return experience;
    }

    /**
     * Creates a transient Experience entity from an UpdateExperienceRequest DTO.
     *
     * @param request The DTO with update data.
     * @return A transient Experience entity.
     */
    public Experience toEntity(UpdateExperienceRequest request) {
        if (request == null) {
            return null;
        }
        Experience experience = new Experience();
        experience.setJobTitle(request.getJobTitle());
        experience.setCompanyName(request.getCompanyName());
        experience.setCompanyUrl(request.getCompanyUrl());
        experience.setCompanyLogoUrl(request.getCompanyLogoUrl());
        experience.setLocation(request.getLocation());
        experience.setLocationType(request.getLocationType());
        experience.setEmploymentType(request.getEmploymentType());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        experience.setAchievements(request.getAchievements());
        experience.setVisible(request.getVisible());
        experience.setDisplayOrder(request.getDisplayOrder());
        return experience;
    }
}