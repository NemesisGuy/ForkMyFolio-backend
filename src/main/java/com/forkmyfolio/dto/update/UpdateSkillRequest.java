package com.forkmyfolio.dto;

import com.forkmyfolio.model.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "UpdateSkillRequest", description = "Request body for updating an existing skill's proficiency level.")
public class UpdateSkillRequest {

    @NotNull(message = "Skill level cannot be null")
    private Skill.SkillLevel level;
}