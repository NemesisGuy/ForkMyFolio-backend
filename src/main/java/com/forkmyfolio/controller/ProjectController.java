package com.forkmyfolio.controller;

import com.forkmyfolio.dto.ProjectDto;
import com.forkmyfolio.mapper.ProjectMapper;
import com.forkmyfolio.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for PUBLICLY viewing portfolio projects.
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Public endpoints for viewing portfolio projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    /**
     * Retrieves all public projects for the portfolio.
     */
    @GetMapping
    @Operation(summary = "Get all public projects", description = "Retrieves a list of all projects for the portfolio.")
    public List<ProjectDto> getPublicProjects() {
        logger.info("Received request to get all public projects.");
        // Note: Your service method `getPublicProjects` already returns a List<ProjectDto>.
        // For consistency with the DTO-less service principle, this should be refactored.
        // I will assume for now it's refactored to return List<Project> and the controller does the mapping.
        List<ProjectDto> projects = projectService.getPublicProjects();
        logger.info("Successfully retrieved {} public projects.", projects.size());
        return projects;
    }

    /**
     * Retrieves a specific public project by its ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a public project by ID", description = "Retrieves a specific project by its ID.")
    public ProjectDto getPublicProjectById(@Parameter(description = "ID of the project") @PathVariable Long id) {
        logger.info("Received request to get public project by ID: {}", id);
        ProjectDto project = projectMapper.toDto(projectService.findProjectEntityById(id));
        logger.info("Successfully retrieved public project with ID: {}", id);
        return project;
    }
}