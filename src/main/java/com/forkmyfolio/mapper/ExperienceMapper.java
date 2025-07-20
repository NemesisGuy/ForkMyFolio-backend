package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateExperienceRequest;
import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.dto.update.UpdateExperienceRequest;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Experience domain objects and their related DTOs.
 */
@Component
public class ExperienceMapper {

    /**
     * Converts an Experience entity to an ExperienceDto for API responses.
     */
    public ExperienceDto toDto(Experience experience) {
        if (experience == null) {
            return null;
        }
        // KEY CHANGE: Use setters to populate the DTO, which avoids constructor issues
        // and correctly maps the entity fields to the DTO fields.
        ExperienceDto dto = new ExperienceDto();
        dto.setUuid(experience.getUuid());
        dto.setJobTitle(experience.getJobTitle());
        dto.setCompanyName(experience.getCompanyName());
        dto.setLocation(experience.getLocation());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setDescription(experience.getDescription());
        // Note: userId, createdAt, and updatedAt are not part of the public DTO.
        return dto;
    }

    /**
     * Converts a CreateExperienceRequest DTO into a new Experience entity.
     */
    public Experience toEntity(CreateExperienceRequest request, User owner) {
        if (request == null) {
            return null;
        }
        Experience experience = new Experience();
        // KEY CHANGE: Use correct setters from the entity.
        experience.setJobTitle(request.getJobTitle());
        experience.setCompanyName(request.getCompanyName());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        experience.setUser(owner);
        return experience;
    }

    /**
     * Applies updates from an UpdateExperienceRequest to an existing Experience entity.
     */
    public void applyUpdateFromRequest(UpdateExperienceRequest request, Experience experience) {
        if (request == null || experience == null) {
            return;
        }
        // KEY CHANGE: Use correct field names from the request DTO.
        request.getJobTitle().ifPresent(experience::setJobTitle);
        request.getCompanyName().ifPresent(experience::setCompanyName);
        request.getLocation().ifPresent(experience::setLocation);
        request.getStartDate().ifPresent(experience::setStartDate);
        request.getEndDate().ifPresent(experience::setEndDate);
        request.getDescription().ifPresent(experience::setDescription);
    }

    /**
     * Converts an ExperienceDto from a backup file into a new Experience entity.
     * This is used by the RestoreService.
     *
     * @param dto   The DTO from the backup.
     * @param owner The User who will own this new experience entry.
     * @return A new Experience entity, ready to be persisted.
     */
    public Experience toEntityFromDto(ExperienceDto dto, User owner) {
        if (dto == null) return null;
        Experience experience = new Experience();
        // Note: We do not set ID or UUID, allowing the DB to generate them.
        // KEY CHANGE: Use correct setters from the entity.
        experience.setJobTitle(dto.getJobTitle());
        experience.setCompanyName(dto.getCompanyName());
        experience.setLocation(dto.getLocation());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setDescription(dto.getDescription());
        experience.setUser(owner);
        return experience;
    }
}