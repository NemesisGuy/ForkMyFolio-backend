package com.forkmyfolio.dto;

import com.forkmyfolio.model.Skill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSkillRequest {

    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 50)
    private String name;

    @NotNull(message = "Skill level cannot be null")
    private Skill.SkillLevel level;
}