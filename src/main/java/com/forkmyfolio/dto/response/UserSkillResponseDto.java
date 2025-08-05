package com.forkmyfolio.dto.response;

import com.forkmyfolio.model.enums.SkillLevel;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing the full details of a UserSkill relationship,
 * returned after a create or update operation.
 */
@Data
public class UserSkillResponseDto {
    private UUID userSkillId;
    private UUID skillId;
    private String name;
    private SkillLevel level;
    private boolean visible;
    private String category;
    private String icon;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}