package com.forkmyfolio.dto.update;

import com.forkmyfolio.model.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "UpdateSkillRequest", description = "Request body for updating an existing skill.")
public class UpdateSkillRequest {

    @NotBlank(message = "Skill name is required.")
    @Size(max = 50)
    @Schema(description = "The name of the skill.", example = "Java", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Skill level is required.")
    @Schema(description = "The proficiency level of the skill.", example = "EXPERT", requiredMode = Schema.RequiredMode.REQUIRED)
    private Skill.SkillLevel level;

    @NotNull(message = "Visibility status is required.")
    @Schema(description = "Whether the skill is visible on the public portfolio.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean visible;

    @Size(max = 100)
    @Schema(description = "The category the skill belongs to.", example = "Backend Development")
    private String category;

    @Size(max = 100)
    @Schema(description = "An icon representing the skill (e.g., a CSS class like 'devicon-java-plain').", example = "devicon-java-plain")
    private String icon;

    @Size(max = 255)
    @Schema(description = "A brief description of the skill.", example = "A popular object-oriented programming language used for enterprise applications.")
    private String description;
}