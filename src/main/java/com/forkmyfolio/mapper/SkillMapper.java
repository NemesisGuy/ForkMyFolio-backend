package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.CreateSkillRequest;
import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.dto.UpdateSkillRequest;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    /**
     * Converts a Skill entity to a SkillDto.
     * This is used for public API responses.
     *
     * @param skill The Skill entity from the database.
     * @return A SkillDto containing public-safe data.
     */
    public SkillDto toDto(Skill skill) {
        if (skill == null) {
            return null;
        }
        SkillDto dto = new SkillDto();
        dto.setUuid(skill.getUuid());
        dto.setName(skill.getName());
        dto.setLevel(skill.getLevel());
        return dto;
    }

    /**
     * Converts a CreateSkillRequest DTO to a new Skill entity.
     * This is used when creating a new skill from an admin request.
     *
     * @param request The DTO containing the new skill's data.
     * @param owner The User who will own this skill.
     * @return A new Skill entity, ready to be persisted.
     */
    public Skill toEntity(CreateSkillRequest request, User owner) {
        if (request == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        skill.setUser(owner);
        return skill;
    }

    /**
     * Applies updates from an UpdateSkillRequest DTO to an existing Skill entity.
     * @param request The DTO with the new level.
     * @param skill The entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateSkillRequest request, Skill skill) {
        if (request == null || skill == null) return;

        if (request.getLevel() != null) {
            skill.setLevel(request.getLevel());
        }
    }
}