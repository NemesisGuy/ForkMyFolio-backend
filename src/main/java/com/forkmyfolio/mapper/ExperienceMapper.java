package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateExperienceRequest;
import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.dto.update.UpdateExperienceRequest;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Mapper class responsible for converting between Experience domain models and Experience-related DTOs.
 * This keeps the conversion logic separate from the service and controller layers.
 */
@Component
@RequiredArgsConstructor
public class ExperienceMapper {

    private final SkillMapper skillMapper;

    /**
     * Converts an Experience entity to an ExperienceDto for API responses.
     *
     * @param experience The Experience entity to convert.
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
            // This now correctly calls the new toDto(Skill) method in SkillMapper
            dto.setSkills(experience.getSkills().stream()
                    .map(skillMapper::toDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setSkills(Collections.emptySet());
        }

        return dto;
    }

    /**
     * Converts a CreateExperienceRequest DTO to a new Experience entity.
     * The associated skills are handled separately in the service layer.
     *
     * @param request The DTO containing the creation data.
     * @param user    The user who will own this experience.
     * @return A new Experience entity, ready to be persisted.
     */
    public Experience toEntity(CreateExperienceRequest request, User user) {
        if (request == null) {
            return null;
        }
        Experience experience = new Experience();
        experience.setUser(user); // Set owner
        return updateEntityFromRequest(experience, request);
    }

    /**
     * Converts an ExperienceDto (typically from a backup) to a new Experience entity.
     *
     * @param dto  The DTO containing the experience data.
     * @param user The user who will own this experience.
     * @return A new Experience entity, ready to be persisted.
     */
    public Experience toEntityFromDto(ExperienceDto dto, User user) {
        if (dto == null) {
            return null;
        }
        Experience experience = new Experience();
        // FIX: Preserve the original UUID from the backup DTO. This is crucial for restores.
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
        // Note: Skills are not mapped here; they are restored separately and then linked in the service.
        return experience;
    }

    /**
     * Creates a transient Experience entity from an UpdateExperienceRequest DTO.
     * This object is used by the service layer to update a persisted entity.
     *
     * @param request The DTO containing the update data.
     * @return A transient Experience entity populated with data from the request.
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

    private Experience updateEntityFromRequest(Experience experience, CreateExperienceRequest request) {
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
}