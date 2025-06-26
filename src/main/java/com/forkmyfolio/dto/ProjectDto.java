package com.forkmyfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for representing Project information in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    /**
     * Unique identifier for the project.
     */
    private Long id;

    /**
     * Title of the project.
     */
    private String title;

    /**
     * Detailed description of the project.
     */
    private String description;

    /**
     * List of technologies or tools used in the project.
     */
    private List<String> techStack;

    /**
     * URL to the project's code repository.
     */
    private String repoUrl;

    /**
     * URL to the live deployment of the project.
     */
    private String liveUrl;

    /**
     * URL to an image representing the project.
     */
    private String imageUrl;

    /**
     * The ID of the user who owns this project.
     */
    private Long userId; // To associate project with a user

    /**
     * Timestamp of when the project was created.
     */
    private LocalDateTime createdAt;
}
