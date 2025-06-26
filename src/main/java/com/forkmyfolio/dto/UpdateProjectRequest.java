package com.forkmyfolio.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Optional;

/**
 * Data Transfer Object for requests to update an existing project.
 * All fields are optional, allowing partial updates.
 * Validation constraints ensure that if a value is provided, it is valid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    /**
     * New title for the project.
     * If provided, must be between 3 and 100 characters.
     */
    @Size(min = 3, max = 100, message = "Project title must be between 3 and 100 characters")
    private Optional<String> title = Optional.empty();

    /**
     * New detailed description for the project.
     * If provided, must be between 10 and 2000 characters.
     */
    @Size(min = 10, max = 2000, message = "Project description must be between 10 and 2000 characters")
    private Optional<String> description = Optional.empty();

    /**
     * New list of technologies or tools used in the project.
     * If provided, replaces the existing list.
     */
    private Optional<List<String>> techStack = Optional.empty();

    /**
     * New URL to the project's code repository.
     * If provided, must be a valid URL.
     */
    @URL(message = "Repository URL must be a valid URL")
    private Optional<String> repoUrl = Optional.empty();

    /**
     * New URL to the live deployment of the project.
     * If provided, must be a valid URL.
     */
    @URL(message = "Live URL must be a valid URL")
    private Optional<String> liveUrl = Optional.empty();

    /**
     * New URL to an image representing the project.
     * If provided, must be a valid URL.
     */
    @URL(message = "Image URL must be a valid URL")
    private Optional<String> imageUrl = Optional.empty();
}
