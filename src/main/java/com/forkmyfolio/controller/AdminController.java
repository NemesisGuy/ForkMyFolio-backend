package com.forkmyfolio.controller;

import com.forkmyfolio.dto.*;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.SkillService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for all administrative actions, secured for ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')") // Secures all methods in this controller
@Tag(name = "Admin", description = "Endpoints for portfolio content management (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final ProjectService projectService;
    private final UserService userService;
    private final SkillService skillService;
    private final UserMapper userMapper;

    @Autowired
    public AdminController(ProjectService projectService,
                           UserService userService,
                           SkillService skillService,
                           UserMapper userMapper) {
        this.projectService = projectService;
        this.userService = userService;
        this.skillService = skillService;
        this.userMapper = userMapper;
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project", description = "Creates a new project for the portfolio.")
    public ProjectDto createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is creating a new project.", currentUser.getEmail());
        logger.debug("Create project request body: {}", createProjectRequest);

        ProjectDto createdProject = projectService.createProject(createProjectRequest, currentUser);
        logger.info("ADMIN User '{}' successfully created project with new ID: {}", currentUser.getEmail(), createdProject.getId());
        return createdProject;
    }

    @PutMapping("/projects/{id}")
    @Operation(summary = "Update an existing project", description = "Updates an existing project by its ID.")
    public ProjectDto updateProject(@PathVariable Long id,
                                    @Valid @RequestBody UpdateProjectRequest updateProjectRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is updating project with ID: {}", currentUser.getEmail(), id);
        logger.debug("Update project request body for ID {}: {}", id, updateProjectRequest);

        ProjectDto updatedProject = projectService.updateProject(id, updateProjectRequest, currentUser);
        logger.info("ADMIN User '{}' successfully updated project with ID: {}", currentUser.getEmail(), id);
        return updatedProject;
    }

    @DeleteMapping("/projects/{id}")
    @Operation(summary = "Delete a project", description = "Deletes a project by its ID.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is deleting project with ID: {}", currentUser.getEmail(), id);

        projectService.deleteProject(id, currentUser);
        logger.info("ADMIN User '{}' successfully deleted project with ID: {}", currentUser.getEmail(), id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new skill", description = "Creates a new skill for the portfolio.")
    public SkillDto createSkill(@Valid @RequestBody CreateSkillRequest createSkillRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is creating a new skill.", currentUser.getEmail());
        logger.debug("Create skill request body: {}", createSkillRequest);

        SkillDto createdSkill = skillService.createSkill(createSkillRequest, currentUser);
        logger.info("ADMIN User '{}' successfully created skill with new ID: {}", currentUser.getEmail(), createdSkill.getId());
        return createdSkill;
    }

    @DeleteMapping("/skills/{id}")
    @Operation(summary = "Delete a skill", description = "Deletes a skill by its ID.")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is deleting skill with ID: {}", currentUser.getEmail(), id);

        skillService.deleteSkill(id, currentUser);
        logger.info("ADMIN User '{}' successfully deleted skill with ID: {}", currentUser.getEmail(), id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current admin user profile", description = "Fetches the profile information for the currently authenticated admin user.")
    public UserDto getAdminProfile() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested their own profile.", currentUser.getEmail());
        return userMapper.toDto (currentUser);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update admin user profile")
    public UserDto updateAdminProfile(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is updating their profile.", currentUser.getEmail());
        logger.debug("Update profile request body: {}", updateUserRequest);

        User updatedUser = userService.updateUser(
                currentUser.getId(),
                updateUserRequest.getFirstName(),
                updateUserRequest.getLastName(),
                updateUserRequest.getProfileImageUrl()
        );

        logger.info("ADMIN User '{}' successfully updated their profile.", currentUser.getEmail());
        return userMapper.toDto(updatedUser);
    }
}
