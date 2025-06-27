package com.forkmyfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a skill that a user possesses, along with their proficiency level.
 */
@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    /**
     * Unique identifier for the skill.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the skill (e.g., "Java", "Spring Boot", "Project Management").
     * Cannot be blank.
     */
    @NotBlank(message = "Skill name cannot be blank")
    @Size(min = 2, max = 50, message = "Skill name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Proficiency level of the skill.
     * Defined by the {@link SkillLevel} enum.
     */
    @NotNull(message = "Skill level cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel level;

    /**
     * The user who possesses this skill.
     * This establishes a many-to-one relationship with the User entity.
     * It is lazily fetched by default.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Many skills can belong to one user
    @JoinColumn(name = "user_id") // Foreign key in the skills table
    private User user;

    /**
     * Timestamp of when the skill record was created.
     * Automatically set by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the skill record was last updated.
     * Automatically set by Hibernate on update.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enum defining the possible proficiency levels for a skill.
     */
    public enum SkillLevel {
        /**
         * Basic understanding and ability to perform tasks with supervision.
         */
        BEGINNER,

        /**
         * Good working knowledge and ability to perform tasks independently.
         */
        INTERMEDIATE,

        /**
         * Advanced knowledge and ability to lead or teach others.
         */
        EXPERT
    }
}
