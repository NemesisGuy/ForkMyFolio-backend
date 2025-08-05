package com.forkmyfolio.dto.update;

import com.forkmyfolio.model.enums.SkillLevel;
import lombok.Data;

/**
 * DTO for updating an existing UserSkill relationship.
 * Fields are optional, allowing for partial updates.
 */
@Data
public class UpdateUserSkillRequest {

    private SkillLevel level;

    private Boolean visible;

    private String description;
}