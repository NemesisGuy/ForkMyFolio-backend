package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Skill domain objects and their related DTOs.
 */
@Component
public class SkillMapper {

    /**
     * Converts a Skill entity to a SkillDto for API responses.
     *
     * @param skill The Skill entity.
     * @return The corresponding SkillDto.
     */
    public SkillDto toDto(Skill skill) {
        if (skill == null) {
            return null;
        }
        // Corrected to use the 'level' field from the entity
        return new SkillDto(skill.getUuid(), skill.getName(), skill.getLevel());
    }

    /**
     * Converts a CreateSkillRequest DTO into a new Skill entity.
     *
     * @param request The DTO containing the new skill data.
     * @param owner   The User who will own this new skill.
     * @return A new Skill entity, ready to be persisted.
     */
    public Skill toEntity(CreateSkillRequest request, User owner) {
        if (request == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setName(request.getName());
        // Corrected to use the 'level' field
        skill.setLevel(request.getLevel());
        // Corrected to link to the User, not PortfolioProfile
        skill.setUser(owner);
        return skill;
    }

    /**
     * Converts a SkillDto from a backup file into a new Skill entity.
     * This method is called by the central RestoreService.
     *
     * @param dto   The DTO from the backup.
     * @param owner The User who will own this new skill.
     * @return A new Skill entity, ready to be persisted.
     */
    public Skill toEntityFromDto(SkillDto dto, User owner) {
        if (dto == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setName(dto.getName());
        // Corrected to use the 'level' field from the DTO
        skill.setLevel(dto.getLevel());
        // Corrected to link to the User, not PortfolioProfile
        skill.setUser(owner);
        return skill;
    }
}