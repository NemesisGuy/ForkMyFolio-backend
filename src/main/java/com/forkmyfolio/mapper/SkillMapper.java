package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting between Skill domain models and Skill-related DTOs.
 * This keeps the conversion logic separate from the service and controller layers.
 */
@Component
public class SkillMapper {

    /**
     * Converts a Skill entity to a SkillDto for API responses.
     *
     * @param skill The Skill entity to convert.
     * @return The corresponding SkillDto.
     */
    public SkillDto toDto(Skill skill) {
        if (skill == null) {
            return null;
        }
        SkillDto dto = new SkillDto();
        dto.setUuid(skill.getUuid());
        dto.setName(skill.getName());
        dto.setLevel(skill.getLevel());
        dto.setVisible(skill.isVisible());
        dto.setCategory(skill.getCategory());
        dto.setIcon(skill.getIcon());
        dto.setDescription(skill.getDescription());
        dto.setCreatedAt(skill.getCreatedAt());
        dto.setUpdatedAt(skill.getUpdatedAt());
        return dto;
    }

    /**
     * Converts a CreateSkillRequest DTO to a new Skill entity.
     *
     * @param request The DTO containing the creation data.
     * @param user    The user who will own this skill.
     * @return A new Skill entity, ready to be persisted.
     */
    public Skill toEntity(CreateSkillRequest request, User user) {
        if (request == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        skill.setVisible(request.isVisible());
        skill.setCategory(request.getCategory());
        skill.setIcon(request.getIcon());
        skill.setDescription(request.getDescription());
        skill.setUser(user); // Set the owner
        return skill;
    }

    /**
     * Converts a SkillDto (typically from a backup) to a new Skill entity.
     *
     * @param dto  The DTO containing the skill data.
     * @param user The user who will own this skill.
     * @return A new Skill entity, ready to be persisted.
     */
    public Skill toEntityFromDto(SkillDto dto, User user) {
        if (dto == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setUser(user);
        skill.setName(dto.getName());
        skill.setLevel(dto.getLevel());
        skill.setVisible(dto.isVisible());
        skill.setCategory(dto.getCategory());
        skill.setIcon(dto.getIcon());
        skill.setDescription(dto.getDescription());
        return skill;
    }
}