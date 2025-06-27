package com.forkmyfolio.controller;

import com.forkmyfolio.dto.CreateProjectRequest;
import com.forkmyfolio.dto.ProjectDto;
import com.forkmyfolio.dto.UpdateProjectRequest;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing portfolio projects.
 * Provides endpoints for CRUD operations on projects.
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Endpoints for managing portfolio projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService; // To get the current user for create/update/delete operations

    /**
     * Constructs a ProjectController with necessary services.
     *
     * @param projectService Service for project-related operations.
     * @param userService    Service for user-related operations.
     */
    @Autowired
    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    /**
     * Retrieves all projects. Publicly accessible.
     *
     * @return ResponseEntity containing a list of {@link ProjectDto}.
     */
    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieves a list of all projects.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of projects",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public List<ProjectDto> getAllProjects() {
        return projectService.getAllProjects();
    }

    /**
     * Retrieves a specific project by its ID. Publicly accessible.
     *
     * @param id The ID of the project to retrieve.
     * @return ResponseEntity containing the {@link ProjectDto} or 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by its ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved project",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public ProjectDto getProjectById(@Parameter(description = "ID of the project to be retrieved") @PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    /**
     * Creates a new project. Admin access required.
     *
     * @param createProjectRequest DTO containing details for the new project.
     * @return ResponseEntity containing the created {@link ProjectDto} and HTTP status 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new project (Admin only)",
            description = "Creates a new project. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role")
    })
    @ResponseStatus(HttpStatus.CREATED) // Indicate 201 status
    public ProjectDto createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return projectService.createProject(createProjectRequest, currentUser);
    }

    /**
     * Updates an existing project. Admin access required.
     *
     * @param id                   The ID of the project to update.
     * @param updateProjectRequest DTO containing updated details for the project.
     * @return ResponseEntity containing the updated {@link ProjectDto}.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing project (Admin only)",
            description = "Updates an existing project by its ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public ProjectDto updateProject(@Parameter(description = "ID of the project to be updated") @PathVariable Long id,
                                    @Valid @RequestBody UpdateProjectRequest updateProjectRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser(); // For authorization context if needed in service
        return projectService.updateProject(id, updateProjectRequest, currentUser);
    }

    /**
     * Deletes a project by its ID. Admin access required.
     *
     * @param id The ID of the project to delete.
     * @return ResponseEntity with HTTP status 200 and a success message.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project (Admin only)",
            description = "Deletes a project by its ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public ResponseEntity<Void> deleteProject(@Parameter(description = "ID of the project to be deleted") @PathVariable Long id) {
        User currentUser = userService.getCurrentAuthenticatedUser(); // For authorization context
        projectService.deleteProject(id, currentUser);
        // For DELETE operations, typically a 204 No Content is returned if successful without a body.
        // If a message is strictly required, the previous approach of returning a Map was okay,
        // but 204 is more standard for DELETE success.
        // The ApiResponseWrapperAdvice is configured to ignore Void/ResponseEntity<Void> return types.
        return ResponseEntity.noContent().build();
    }
}
