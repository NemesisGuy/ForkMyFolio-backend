package com.forkmyfolio.service;

import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for business logic related to Projects.
 * This service operates solely on domain models and is DTO-agnostic.
 */
public interface ProjectService {

    /**
     * Retrieves the list of public projects for the portfolio owner.
     * @return A list of {@link Project} entities.
     */
    List<Project> getPublicProjects();

    /**
     * Retrieves a single project by its public UUID.
     * @param uuid The UUID of the project.
     * @return The {@link Project} entity.
     */
    Project getProjectByUuid(UUID uuid);

    /**
     * Creates and persists a new project.
     * @param project The pre-constructed project entity to save.
     * @return The persisted {@link Project} entity.
     */
    Project createProject(Project project);

    /**
     * Saves an updated project entity.
     * @param project The project entity with updated fields to be saved.
     * @return The updated and persisted {@link Project} entity.
     */
    Project save(Project project);

    /**
     * Deletes a project by its public UUID.
     * @param uuid The UUID of the project to delete.
     * @param currentUser The user performing the action.
     * @throws AccessDeniedException if the user is not authorized.
     */
    void deleteProject(UUID uuid, User currentUser);
}