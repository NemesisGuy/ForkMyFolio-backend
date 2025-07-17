package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.mapper.ProjectMapper;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.VisitorStatsService;
import com.forkmyfolio.util.SecurityUtils;
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
import java.util.UUID;
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
    private final VisitorStatsService visitorStatsService;
    private final SecurityUtils securityUtils;

    @Autowired
    public ProjectController(ProjectService projectService, ProjectMapper projectMapper, VisitorStatsService visitorStatsService, SecurityUtils securityUtils) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
        this.visitorStatsService = visitorStatsService;
        this.securityUtils = securityUtils;
    }

    /**
     * Retrieves all public projects for the portfolio.
     */
    @GetMapping
    @Operation(summary = "Get all public projects")
    @TrackVisitor(VisitorStatType.PROJECTS_SECTION_VIEW)
    public List<ProjectDto> getPublicProjects() {
        logger.info("Received request to get all public projects.");

        // FIX: Use the mapper to convert the list of entities to a list of DTOs
        List<Project> projectEntities = projectService.getPublicProjects();
        List<ProjectDto> projectDtos = projectEntities.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());

        logger.info("Successfully retrieved {} public projects.", projectDtos.size());
        return projectDtos;
    }

    /**
     * Retrieves a specific public project by its UUID.
     */
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a public project by its UUID")
    @TrackVisitor(VisitorStatType.PROJECT_VIEW)
    public ProjectDto getPublicProjectByUuid(@Parameter(description = "UUID of the project") @PathVariable UUID uuid) {
        logger.info("Received request to get public project by UUID: {}", uuid);

        // FIX: Call the correct service method and use the mapper
        Project projectEntity = projectService.getProjectByUuid(uuid);

        logger.info("Successfully retrieved public project with UUID: {}", uuid);
        return projectMapper.toDto(projectEntity);
    }
}