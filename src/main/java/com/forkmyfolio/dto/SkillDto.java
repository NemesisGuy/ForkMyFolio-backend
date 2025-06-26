package com.forkmyfolio.dto;

import com.forkmyfolio.model.Skill.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for representing Skill information in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {

    /**
     * Unique identifier for the skill.
     */
    private Long id;

    /**
     * Name of the skill (e.g., "Java", "Spring Boot").
     */
    private String name;

    /**
     * Proficiency level of the skill (e.g., BEGINNER, INTERMEDIATE, EXPERT).
     */
    private SkillLevel level;

    /**
     * The ID of the user who possesses this skill.
     */
    private Long userId; // To associate skill with a user
}
