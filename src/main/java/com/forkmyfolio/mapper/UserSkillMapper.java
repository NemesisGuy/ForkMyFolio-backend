package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.UserSkillResponseDto;
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
     * Manually maps a UserSkill entity to its corresponding response DTO.
     *
     * @param userSkill The persisted UserSkill entity.
     * @return A UserSkillResponseDto.
     */
    public UserSkillResponseDto toResponseDto(UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }

        UserSkillResponseDto dto = new UserSkillResponseDto();
        // FIX: Use the public-facing UUID, not the internal Long ID.
        // This adheres to the architectural rule of never exposing internal database keys.
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
     * Manually maps a list of UserSkill entities to a list of response DTOs.
     *
     * @param userSkills The list of persisted UserSkill entities.
     * @return A list of UserSkillResponseDtos.
     */
    public List<UserSkillResponseDto> toResponseDtoList(List<UserSkill> userSkills) {
        if (userSkills == null || userSkills.isEmpty()) {
            return Collections.emptyList();
        }
        return userSkills.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}