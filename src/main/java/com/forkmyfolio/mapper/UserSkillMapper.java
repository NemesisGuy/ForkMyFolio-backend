package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.UserSkillDto;
import com.forkmyfolio.model.UserSkill;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting UserSkill entities to their corresponding DTOs.
 * This class performs manual mapping to adhere to project standards.
 */
@Component
public class UserSkillMapper {

    /**
     * Manually maps a UserSkill entity to its corresponding DTO.
     *
     * @param userSkill The persisted UserSkill entity.
     * @return A UserSkillDto.
     */
    public UserSkillDto toDto(UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }

        UserSkillDto dto = new UserSkillDto();
        dto.setUserSkillId(userSkill.getUuid());
        dto.setLevel(userSkill.getLevel());
        dto.setVisible(userSkill.isVisible());
        dto.setDescription(userSkill.getDescription());
        dto.setCreatedAt(userSkill.getCreatedAt());
        dto.setUpdatedAt(userSkill.getUpdatedAt());

        // Safely map properties from the related Skill entity
        if (userSkill.getSkill() != null) {
            dto.setSkillId(userSkill.getSkill().getUuid());
            dto.setName(userSkill.getSkill().getName());
            dto.setCategory(userSkill.getSkill().getCategory());
            dto.setIcon(userSkill.getSkill().getIcon());
        }

        return dto;
    }

    /**
     * Manually maps a list of UserSkill entities to a list of DTOs.
     *
     * @param userSkills The list of persisted UserSkill entities.
     * @return A list of UserSkillDtos.
     */
    public List<UserSkillDto> toDtoList(List<UserSkill> userSkills) {
        if (userSkills == null || userSkills.isEmpty()) {
            return Collections.emptyList();
        }
        return userSkills.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a UserSkillDto (typically from a backup) to a new, transient UserSkill entity.
     * <p>
     * Note: This method only maps the direct properties from the DTO. The service layer is
     * responsible for setting the associated {@link com.forkmyfolio.model.User} and
     * {@link com.forkmyfolio.model.Skill} entities before persisting.
     *
     * @param dto The DTO containing the user-skill data.
     * @return A new, transient UserSkill entity.
     */
    public UserSkill toEntity(UserSkillDto dto) {
        if (dto == null) {
            return null;
        }
        UserSkill userSkill = new UserSkill();

        // Preserve the original UUID from the backup DTO.
        // The @PrePersist on the entity will generate a new one if this is null.
        userSkill.setUuid(dto.getUserSkillId());

        userSkill.setLevel(dto.getLevel());
        userSkill.setVisible(dto.isVisible());
        userSkill.setDescription(dto.getDescription());

        return userSkill;
    }
}