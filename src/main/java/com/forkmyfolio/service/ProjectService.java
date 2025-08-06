package com.forkmyfolio.service;

import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing portfolio projects.
 * This service handles the business logic for creating, retrieving,
 * updating, and deleting projects associated with a user.
 */
public interface ProjectService {

    /**
     * Retrieves all projects for a given user, ordered by their display order.
     *
     * @param user The user whose projects are to be retrieved.
     * @return A list of {@link Project} entities.
     */
    List<Project> getProjectsForUser(User user);

    /**
     * Finds a specific project by its UUID and ensures it belongs to the given user.
     *
     * @param uuid The UUID of the project to find.
     * @param user The user who must own the project.
     * @return The found {@link Project} entity.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the project does not exist.
     * @throws org.springframework.security.access.AccessDeniedException if the project does not belong to the user.
     */
    Project findProjectByUuidAndUser(UUID uuid, User user);

    /**
     * Creates a new project and associates it with a set of skills.
     *
     * @param project The project entity to be created (owner must be set).
     * @param skillNames A set of skill names to find or create and associate with the project.
     * @return The newly created and saved {@link Project} entity.
     */
    Project createProject(Project project, Set<String> skillNames);

    /**
     * Updates an existing project with new data and a new set of associated skills.
     *
     * @param uuid The UUID of the project to update.
     * @param updatedProjectData A {@link Project} object containing the new data.
     * @param skillNames The complete new set of skill names to be associated with the project.
     * @param currentUser The user performing the update, for permission checking.
     * @return The updated and saved {@link Project} entity.
     */
    Project updateProject(UUID uuid, Project updatedProjectData, Set<String> skillNames, User currentUser);

    /**
     * Deletes a project, ensuring the user has permission to do so.
     *
     * @param uuid The UUID of the project to delete.
     * @param currentUser The user performing the deletion, for permission checking.
     */
    void deleteProject(UUID uuid, User currentUser);
}