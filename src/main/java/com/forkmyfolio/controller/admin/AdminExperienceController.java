package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.CreateExperienceRequest;
import com.forkmyfolio.dto.ExperienceDto;
import com.forkmyfolio.dto.UpdateExperienceRequest;
import com.forkmyfolio.mapper.ExperienceMapper;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ExperienceService;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/experience")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Experience Management", description = "Endpoints for managing work experience.")
@SecurityRequirement(name = "bearerAuth")
public class AdminExperienceController {

    private static final Logger logger = LoggerFactory.getLogger(AdminExperienceController.class);

    private final ExperienceService experienceService;
    private final UserService userService;
    private final ExperienceMapper experienceMapper;

    @Autowired
    public AdminExperienceController(ExperienceService experienceService, UserService userService, ExperienceMapper experienceMapper) {
        this.experienceService = experienceService;
        this.userService = userService;
        this.experienceMapper = experienceMapper;
    }

    // --- READ (List All) ---
    @GetMapping
    @Operation(summary = "List all experience entries", description = "Retrieves a list of all work experience entries for the admin to manage.")
    public List<ExperienceDto> getAllExperienceForAdmin() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested all experience entries.", currentUser.getEmail());
        List<Experience> experiences = experienceService.getPublicExperience(); // The public list is the same as the admin list
        return experiences.stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- READ (Single by UUID) ---
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a single experience entry by UUID", description = "Retrieves a single work experience entry by its UUID, typically for populating an edit form.")
    public ExperienceDto getExperienceByUuid(@Parameter(description = "The UUID of the experience") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested experience entry with UUID: {}", currentUser.getEmail(), uuid);
        Experience experience = experienceService.getExperienceByUuid(uuid);
        return experienceMapper.toDto(experience);
    }

    // --- CREATE ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new experience entry")
    public ExperienceDto createExperience(@Valid @RequestBody CreateExperienceRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new experience: {}", currentUser.getEmail(), request.getJobTitle());
        Experience newExperience = experienceMapper.toEntity(request, currentUser);
        Experience savedExperience = experienceService.createExperience(newExperience);
        return experienceMapper.toDto(savedExperience);
    }

    // --- UPDATE ---
    @PutMapping("/{uuid}")
    @Operation(summary = "Update an experience entry by UUID")
    public ExperienceDto updateExperience(@Parameter(description = "The UUID of the experience") @PathVariable UUID uuid, @Valid @RequestBody UpdateExperienceRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating experience UUID: {}", currentUser.getEmail(), uuid);
        Experience existingExperience = experienceService.getExperienceByUuid(uuid);
        experienceMapper.applyUpdateFromRequest(request, existingExperience);
        Experience updatedExperience = experienceService.save(existingExperience);
        return experienceMapper.toDto(updatedExperience);
    }

    // --- DELETE ---
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete an experience entry by UUID")
    public ResponseEntity<Void> deleteExperience(@Parameter(description = "The UUID of the experience") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting experience UUID: {}", currentUser.getEmail(), uuid);
        experienceService.deleteExperience(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}