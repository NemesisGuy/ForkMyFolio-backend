package com.forkmyfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * Data Transfer Object for requests to create a new project.
 * Contains all the necessary information for project creation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    /**
     * Title of the project. Cannot be blank.
     */
    @NotBlank(message = "Project title cannot be blank")
    @Size(min = 3, max = 100, message = "Project title must be between 3 and 100 characters")
    private String title;

    /**
     * Detailed description of the project.
     */
    @NotBlank(message = "Project description cannot be blank")
    @Size(min = 10, max = 2000, message = "Project description must be between 10 and 2000 characters")
    private String description;

    /**
     * List of technologies or tools used in the project.
     */
    private List<String> techStack;

    /**
     * URL to the project's code repository (e.g., GitHub, GitLab).
     * Must be a valid URL format if provided.
     */
    @URL(message = "Repository URL must be a valid URL")
    private String repoUrl;

    /**
     * URL to the live deployment of the project, if available.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Live URL must be a valid URL")
    private String liveUrl;

    /**
     * URL to an image representing the project.
     * Must be a valid URL format if provided.
     */
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;
}
