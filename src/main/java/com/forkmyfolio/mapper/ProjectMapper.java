package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Project domain objects and their related DTOs.
 */
@Component
public class ProjectMapper {

    /**
     * Converts a Project entity to a ProjectDto for API responses.
     *
     * @param project The Project entity to convert.
     * @return A complete ProjectDto.
     */
    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }
        // KEY CHANGE: The constructor now includes all fields from the DTO,
        // ensuring the API response is complete.
        return new ProjectDto(
                project.getUuid(),
                project.getTitle(),
                project.getDescription(),
                project.getTechStack(),
                project.getRepoUrl(),
                project.getLiveUrl(),
                project.getImageUrl(),
                project.getUser() != null ? project.getUser().getId() : null, // Safely get user ID
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * Converts a ProjectDto from a backup file into a new Project entity.
     * This is used by the RestoreService.
     *
     * @param dto   The DTO from the backup.
     * @param owner The User who will own this new project.
     * @return A new Project entity, ready to be persisted.
     */
    public Project toEntityFromDto(ProjectDto dto, User owner) {
        if (dto == null) {
            return null;
        }
        Project project = new Project();
        // Note: We do not set ID or UUID, allowing the DB to generate them.
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setTechStack(dto.getTechStack());
        project.setRepoUrl(dto.getRepoUrl());
        project.setLiveUrl(dto.getLiveUrl());
        project.setImageUrl(dto.getImageUrl());
        project.setUser(owner);
        return project;
    }

    /**
     * Converts a CreateProjectRequest DTO into a new Project entity.
     *
     * @param request The DTO with new project data.
     * @param owner   The User who will own this project.
     * @return A new Project entity, ready to be persisted.
     */
    public Project toEntity(CreateProjectRequest request, User owner) {
        if (request == null) {
            return null;
        }
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setRepoUrl(request.getRepoUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setImageUrl(request.getImageUrl());
        project.setUser(owner);
        return project;
    }

    /**
     * Applies updates from an UpdateProjectRequest to an existing Project entity.
     *
     * @param request The DTO with the fields to update.
     * @param project The existing entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateProjectRequest request, Project project) {
        if (request == null || project == null) {
            return;
        }

        request.getTitle().ifPresent(project::setTitle);
        request.getDescription().ifPresent(project::setDescription);
        request.getTechStack().ifPresent(project::setTechStack);
        request.getRepoUrl().ifPresent(project::setRepoUrl);
        request.getLiveUrl().ifPresent(project::setLiveUrl);
        request.getImageUrl().ifPresent(project::setImageUrl);
    }
}