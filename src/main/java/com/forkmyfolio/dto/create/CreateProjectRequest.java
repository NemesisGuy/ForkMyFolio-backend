package com.forkmyfolio.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.Set;
import java.util.UUID;

@Data
@Schema(name = "CreateProjectRequest", description = "Request body for creating a new project.")
public class CreateProjectRequest {

    @NotBlank(message = "Project title cannot be blank")
    @Size(min = 3, max = 100, message = "Project title must be between 3 and 100 characters")
    @Schema(description = "The title of the project.", example = "My Awesome App", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Project description cannot be blank")
    @Size(min = 10, max = 2000, message = "Project description must be between 10 and 2000 characters")
    @Schema(description = "A detailed description of the project.", example = "This application solves a real-world problem by...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @URL(message = "Repository URL must be a valid URL")
    @Schema(description = "URL to the project's code repository (e.g., GitHub).", example = "https://github.com/user/my-awesome-app")
    private String repoUrl;

    @URL(message = "Live URL must be a valid URL")
    @Schema(description = "URL to the live deployment of the project.", example = "https://my-awesome-app.com")
    private String liveUrl;

    @URL(message = "Image URL must be a valid URL")
    @Schema(description = "URL to a preview image for the project.", example = "https://cdn.images.com/my-awesome-app.png")
    private String imageUrl;

    @Schema(description = "Whether the project is visible on the public portfolio.", example = "true")
    private boolean visible = true;

    @Schema(description = "The order in which to display this project.", example = "1")
    private Integer displayOrder = 0;

    @Schema(description = "A set of UUIDs for the skills used in this project.")
    private Set<UUID> skillUuids;
}