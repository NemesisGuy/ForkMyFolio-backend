package com.forkmyfolio.controller;

import com.forkmyfolio.dto.CreateSkillRequest;
import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.dto.response.ApiResponseWrapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.SkillService;
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

import java.util.Collections;
import java.util.List;

/**
 * Controller for managing user skills.
 * Provides endpoints for CRUD operations on skills.
 */
@RestController
@RequestMapping("/api/v1/skills")
@Tag(name = "Skills", description = "Endpoints for managing user skills")
public class SkillController {

    private final SkillService skillService;
    private final UserService userService;

    /**
     * Constructs a SkillController with necessary services.
     * @param skillService Service for skill-related operations.
     * @param userService Service for user-related operations.
     */
    @Autowired
    public SkillController(SkillService skillService, UserService userService) {
        this.skillService = skillService;
        this.userService = userService;
    }

    /**
     * Retrieves all skills. Publicly accessible.
     * This might list all unique skills in the system or skills of the current user
     * depending on refined requirements. For now, lists all skills.
     * @return ResponseEntity containing a list of {@link SkillDto}.
     */
    @GetMapping
    @Operation(summary = "Get all skills", description = "Retrieves a list of all skills.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of skills",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public List<SkillDto> getAllSkills() {
        // Note: Requirement says GET /api/v1/skills. If this should be user-specific,
        // it would need to fetch current user and then get skills by user ID.
        // For now, interpreting as "all skills available in the system" (e.g. for an admin to see, or a public list of skill types).
        // If skills are always user-bound and not global, this might need a rethink or be an admin endpoint.
        // For now, let's assume it lists all skills from all users for public view, or it might be skills of current logged in user
        // If no user logged in, it could be all skills in the system.
        // To be simple and align with "admin only for POST/DELETE", this public GET could list all skills.
        return skillService.getAllSkills();
    }

    /**
     * Retrieves a specific skill by its ID. Publicly accessible.
     * @param id The ID of the skill to retrieve.
     * @return ResponseEntity containing the {@link SkillDto} or 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID", description = "Retrieves a specific skill by its ID.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved skill",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Skill not found",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public SkillDto getSkillById(@Parameter(description = "ID of the skill to be retrieved") @PathVariable Long id) {
        return skillService.getSkillById(id);
    }

    /**
     * Creates a new skill. Admin access required.
     * The skill will be associated with the admin user creating it.
     * @param createSkillRequest DTO containing details for the new skill.
     * @return ResponseEntity containing the created {@link SkillDto} and HTTP status 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new skill (Admin only)",
               description = "Creates a new skill. Requires ADMIN role. The skill is associated with the admin user creating it.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Skill created successfully",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public SkillDto createSkill(@Valid @RequestBody CreateSkillRequest createSkillRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return skillService.createSkill(createSkillRequest, currentUser);
    }

    /**
     * Deletes a skill by its ID. Admin access required.
     * @param id The ID of the skill to delete.
     * @return ResponseEntity with HTTP status 200 and a success message.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a skill (Admin only)",
               description = "Deletes a skill by its ID. Requires ADMIN role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Skill deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Skill not found",
                           content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    public ResponseEntity<Void> deleteSkill(@Parameter(description = "ID of the skill to be deleted") @PathVariable Long id) {
        User currentUser = userService.getCurrentAuthenticatedUser(); // For authorization context
        skillService.deleteSkill(id, currentUser);
        return ResponseEntity.noContent().build(); // Standard 204 No Content for successful DELETE
    }
}
