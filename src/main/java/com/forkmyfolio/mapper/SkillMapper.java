package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.dto.response.UserSkillDto;
import com.forkmyfolio.dto.update.UpdateSkillRequest;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.UserSkill;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Skill/UserSkill entities and their corresponding DTOs.
 */
@Component
public class SkillMapper {

    /**
     * Maps a UserSkill entity to a detailed SkillDto, intended for UI display.
     * This DTO includes comprehensive user-specific details like proficiency level and visibility.
     *
     * @param userSkill The UserSkill entity.
     * @return A detailed SkillDto.
     */
    public SkillDto toDetailDto(UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }

        SkillDto dto = new SkillDto();
        // User-specific details from the UserSkill relationship
        dto.setUserSkillId(userSkill.getUuid());
        dto.setLevel(userSkill.getLevel());
        dto.setVisible(userSkill.isVisible());
        dto.setDescription(userSkill.getDescription());
        dto.setCreatedAt(userSkill.getCreatedAt());
        dto.setUpdatedAt(userSkill.getUpdatedAt());

        // Global details from the underlying Skill
        if (userSkill.getSkill() != null) {
            dto.setSkillId(userSkill.getSkill().getUuid());
            dto.setName(userSkill.getSkill().getName());
            dto.setCategory(userSkill.getSkill().getCategory());
            dto.setIcon(userSkill.getSkill().getIcon());
        }

        return dto;
    }

    /**
     * Maps a global Skill entity to a SkillDto.
     * This is required when displaying skills attached to a Project or Experience,
     * where there is no user-specific context (like level or visibility).
     *
     * @param skill The global Skill entity.
     * @return A lean SkillDto containing only global skill information.
     */
    public SkillDto toDto(Skill skill) {
        if (skill == null) {
            return null;
        }
        SkillDto dto = new SkillDto();
        // Global details from the Skill entity
        dto.setSkillId(skill.getUuid());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory());
        dto.setIcon(skill.getIcon());
        // Note: User-specific fields (userSkillId, level, visible, etc.) are intentionally left null
        // as they do not apply in this context.
        return dto;
    }

    /**
     * Maps a list of UserSkill entities to a list of detailed SkillDtos.
     *
     * @param userSkills The list of UserSkill entities.
     * @return A list of detailed SkillDtos.
     */
    public List<SkillDto> toDetailDtoList(List<UserSkill> userSkills) {
        if (userSkills == null || userSkills.isEmpty()) {
            return Collections.emptyList();
        }
        return userSkills.stream()
                .map(this::toDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps a UserSkill entity to a lean UserSkillDto, intended for backups.
     * This DTO contains only the essential information needed to restore the relationship.
     *
     * @param userSkill The UserSkill entity.
     * @return A lean UserSkillDto for backup purposes.
     */
    public UserSkillDto toBackupDto(UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }

        UserSkillDto dto = new UserSkillDto();
        dto.setUserSkillId(userSkill.getUuid());
        dto.setLevel(userSkill.getLevel());

        if (userSkill.getSkill() != null) {
            dto.setSkillId(userSkill.getSkill().getUuid());
            dto.setName(userSkill.getSkill().getName());
        }

        return dto;
    }

    /**
     * Maps a list of UserSkill entities to a list of lean UserSkillDtos for backups.
     *
     * @param userSkills The list of UserSkill entities.
     * @return A list of lean UserSkillDtos.
     */
    public List<UserSkillDto> toBackupDtoList(List<UserSkill> userSkills) {
        if (userSkills == null || userSkills.isEmpty()) {
            return Collections.emptyList();
        }
        return userSkills.stream()
                .map(this::toBackupDto)
                .collect(Collectors.toList());
    }

    /**
     * FIX: Implement the missing method to apply updates from a DTO to an entity.
     * This method modifies the entity in-place.
     *
     * @param request The DTO containing the update data.
     * @param entity  The UserSkill entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateSkillRequest request, UserSkill entity) {
        if (request == null || entity == null) {
            return;
        }
        // Only update fields that are provided in the request to avoid unintentional nulling.
        if (request.getLevel() != null) {
            entity.setLevel(request.getLevel());
        }

            entity.setVisible(request.isVisible());

        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
    }
}