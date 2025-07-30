package com.forkmyfolio.dto.response;

import com.forkmyfolio.model.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(name = "SkillDto", description = "Represents a skill belonging to a user.")
public class SkillDto {

    @Schema(description = "The unique identifier for the skill.", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uuid;

    @Schema(description = "The name of the skill.", example = "Spring Boot")
    private String name;

    @Schema(description = "The proficiency level of the skill.", example = "ADVANCED")
    private Skill.SkillLevel level;

    @Schema(description = "Whether the skill is visible on the public portfolio.", example = "true")
    private boolean visible;

    @Schema(description = "The category the skill belongs to.", example = "Backend Frameworks")
    private String category;

    @Schema(description = "An icon representing the skill (e.g., a CSS class like 'devicon-spring-plain').", example = "devicon-spring-plain")
    private String icon;

    @Schema(description = "A brief description of the skill.", example = "Framework for building stand-alone, production-grade Spring-based Applications.")
    private String description;

    @Schema(description = "The timestamp when the skill was created.")
    private LocalDateTime createdAt;

    @Schema(description = "The timestamp when the skill was last updated.")
    private LocalDateTime updatedAt;
}