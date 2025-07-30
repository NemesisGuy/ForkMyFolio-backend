package com.forkmyfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a project in a user's portfolio.
 */
@NamedEntityGraph(
        name = "Project.withSkills",
        attributeNodes = @NamedAttributeNode("skills")
)
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    /**
     * Unique identifier for the project.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    /**
     * Title of the project. Cannot be blank.
     */
    @NotBlank(message = "Project title cannot be blank")
    @Size(min = 3, max = 100, message = "Project title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of the project.
     * Stored as TEXT for longer content.
     */
    @NotBlank(message = "Project description cannot be blank")
    @Size(min = 10, max = 2000, message = "Project description must be between 10 and 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The skills and technologies used in the project.
     * This establishes a many-to-many relationship with the Skill entity,
     * replacing the old plain text techStack.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_skills",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    /**
     * URL to the project's code repository (e.g., GitHub, GitLab).
     * Must be a valid URL format.
     */
    @URL(message = "Repository URL must be a valid URL")
    private String repoUrl;

    /**
     * URL to the live deployment of the project, if available.
     * Must be a valid URL format.
     */
    @URL(message = "Live URL must be a valid URL")
    private String liveUrl;

    /**
     * URL to an image representing the project.
     * Must be a valid URL format.
     */
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    /**
     * The user who owns this project.
     * This establishes a many-to-one relationship with the User entity.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Flag to control the visibility of this project on the public portfolio.
     */
    @Column(nullable = false)
    private boolean visible = true;

    /**
     * The order in which this project should be displayed on the portfolio.
     * Lower numbers appear first.
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Timestamp of when the project was created.
     * Automatically set by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the project was last updated.
     * Automatically set by Hibernate on update.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}