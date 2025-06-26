package com.forkmyfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a project in a user's portfolio.
 */
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
     * List of technologies or tools used in the project.
     * For example, ["Java", "Spring Boot", "React"].
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_tech_stack", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "technology")
    private List<String> techStack;

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
     * It is eagerly fetched as project details often require user information.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Many projects can belong to one user
    @JoinColumn(name = "user_id") // Foreign key in the projects table
    private User user;


    /**
     * Timestamp of when the project was created.
     * Automatically set by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
