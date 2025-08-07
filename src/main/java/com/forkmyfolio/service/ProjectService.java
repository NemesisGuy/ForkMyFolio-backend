package com.forkmyfolio.service;

import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing a user's portfolio projects.
 * This service operates exclusively on domain entities.
 */
public interface ProjectService {

    /**
     * Retrieves all projects for a specific user, ordered by displayOrder.
     */
    List<Project> getProjectsForUser(User user);

    /**
     * Finds a single project by its UUID, ensuring it belongs to the specified user.
     */
    Project findProjectByUuidAndUser(UUID uuid, User user);

    /**
     * Creates a new project for a user and associates it with the given skills.
     */
    Project createProject(Project project, Set<String> skillNames);

    /**
     * Updates an existing project.
     */
    Project updateProject(UUID uuid, Project projectWithUpdates, Set<String> skillNames, User user);

    /**
     * Deletes a project, verifying ownership first.
     */
    void deleteProject(UUID uuid, User user);
}