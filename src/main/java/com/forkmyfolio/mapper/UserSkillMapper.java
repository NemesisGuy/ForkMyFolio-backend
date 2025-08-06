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
}