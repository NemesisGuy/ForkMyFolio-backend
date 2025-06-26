package com.forkmyfolio.dto;

import com.forkmyfolio.model.Skill.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for requests to create a new skill.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSkillRequest {

    /**
     * Name of the skill. Cannot be blank.
     */
    @NotBlank(message = "Skill name cannot be blank")
    @Size(min = 2, max = 50, message = "Skill name must be between 2 and 50 characters")
    private String name;

    /**
     * Proficiency level of the skill. Cannot be null.
     */
    @NotNull(message = "Skill level cannot be null")
    private SkillLevel level;
}
