package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    /**
     * Converts a Project entity to a ProjectDto.
     * @param project The Project entity to convert.
     * @return The corresponding ProjectDto.
     */
    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }
        return new ProjectDto(
                project.getUuid(),
                project.getTitle(),
                project.getDescription(),
                project.getTechStack(),
                project.getRepoUrl(),
                project.getLiveUrl(),
                project.getImageUrl(),
                project.getUser() != null ? project.getUser().getId() : null,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * Converts a CreateProjectRequest DTO to a new Project entity.
     * @param request The DTO containing creation data.
     * @param owner The User who will own this project.
     * @return A new Project entity, not yet persisted.
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
     * Applies updates from an UpdateProjectRequest DTO to an existing Project entity.
     * @param request The DTO containing update data.
     * @param project The Project entity to be updated.
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