package com.forkmyfolio.dto.create;

import com.forkmyfolio.model.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "CreateSkillRequest", description = "Request body for creating a new skill.")
public class CreateSkillRequest {

    @NotBlank(message = "Skill name is required.")
    @Size(max = 50)
    @Schema(description = "The name of the skill.", example = "Java", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Skill level is required.")
    @Schema(description = "The proficiency level of the skill.", example = "ADVANCED", requiredMode = Schema.RequiredMode.REQUIRED)
    private Skill.SkillLevel level;

    @Schema(description = "Whether the skill is visible on the public portfolio.", example = "true")
    private boolean visible = true;

    @Size(max = 100)
    @Schema(description = "The category the skill belongs to.", example = "Programming")
    private String category;

    @Size(max = 100)
    @Schema(description = "An icon representing the skill (e.g., a CSS class like 'devicon-java-plain').", example = "devicon-java-plain")
    private String icon;

    @Size(max = 255)
    @Schema(description = "A brief description of the skill.", example = "A popular object-oriented programming language.")
    private String description;
}