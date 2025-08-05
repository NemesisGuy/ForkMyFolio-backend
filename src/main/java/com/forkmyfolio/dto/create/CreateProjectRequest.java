package com.forkmyfolio.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.HashSet;
import java.util.Set;

@Data
@Schema(name = "CreateProjectRequest", description = "Data required to create a new project.")
public class CreateProjectRequest {

    @NotBlank(message = "Project title cannot be blank")
    @Size(min = 3, max = 100, message = "Project title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Project description cannot be blank")
    @Size(min = 10, max = 2000, message = "Project description must be between 10 and 2000 characters")
    private String description;

    @URL(message = "Repository URL must be a valid URL")
    private String repoUrl;

    @URL(message = "Live URL must be a valid URL")
    private String liveUrl;

    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    @Schema(description = "Controls the visibility of this project on the public portfolio.", defaultValue = "true")
    private boolean visible = true;

    @Schema(description = "The order in which this project should be displayed. Lower numbers appear first.", defaultValue = "0")
    private Integer displayOrder = 0;

    @Schema(description = "A set of skill names to associate with this project. New skills will be created if they don't exist globally.", example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\"]")
    private Set<String> skills = new HashSet<>();
}