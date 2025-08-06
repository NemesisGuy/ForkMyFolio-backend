package com.forkmyfolio.controller.management;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.create.CreateExperienceRequest;
import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.dto.update.UpdateExperienceRequest;
import com.forkmyfolio.mapper.ExperienceMapper;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.service.ExperienceService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/me/experiences")
@Tag(name = "Experience Management (Me)", description = "Endpoints for the authenticated user to manage their own work experiences.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ExperienceManagementController {

    private final ExperienceService experienceService;
    private final UserService userService;
    private final ExperienceMapper experienceMapper;

    @GetMapping
    @Operation(summary = "Get all of my experiences")
    public ResponseEntity<ApiResponseWrapper<List<ExperienceDto>>> getMyExperiences() {
        // Fetch the user with all portfolio data to provide context for skill mapping.
        User currentUser = userService.getCurrentAuthenticatedUserWithAllPortfolioData();
        List<Experience> experiences = experienceService.getExperiencesForUser(currentUser);

        // Create the skill lookup map for context.
        Map<UUID, UserSkill> skillLookup = currentUser.getUserSkills().stream()
                .collect(Collectors.toMap(us -> us.getSkill().getUuid(), us -> us));

        // Use the context-aware mapper to get rich skill details.
        List<ExperienceDto> experienceDtos = experiences.stream()
                .map(experience -> experienceMapper.toDto(experience, skillLookup))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponseWrapper<>(experienceDtos));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get one of my experiences by its UUID")
    public ResponseEntity<ApiResponseWrapper<ExperienceDto>> getMyExperienceByUuid(@PathVariable UUID uuid) {
        // Fetch the user with all portfolio data to provide context for skill mapping.
        User currentUser = userService.getCurrentAuthenticatedUserWithAllPortfolioData();
        Experience experience = experienceService.findExperienceByUuidAndUser(uuid, currentUser);

        // Create the skill lookup map for context.
        Map<UUID, UserSkill> skillLookup = currentUser.getUserSkills().stream()
                .collect(Collectors.toMap(us -> us.getSkill().getUuid(), us -> us));

        // Use the context-aware mapper to get rich skill details.
        ExperienceDto experienceDto = experienceMapper.toDto(experience, skillLookup);
        return ResponseEntity.ok(new ApiResponseWrapper<>(experienceDto));
    }

    @PostMapping
    @Operation(summary = "Create a new experience for myself")
    public ResponseEntity<ApiResponseWrapper<ExperienceDto>> createMyExperience(@Valid @RequestBody CreateExperienceRequest createRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // Controller uses mapper to create a transient entity from the DTO.
        Experience newExperienceDetails = experienceMapper.toEntity(createRequest);

        // Controller passes the transient entity and owner to the service.
        Experience createdExperience = experienceService.createExperience(newExperienceDetails, createRequest.getSkills(), currentUser);

        // Map the persisted entity back to a DTO for the response.
        ExperienceDto createdDto = experienceMapper.toDto(createdExperience);
        return new ResponseEntity<>(new ApiResponseWrapper<>(createdDto), HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update one of my experiences")
    public ResponseEntity<ApiResponseWrapper<ExperienceDto>> updateMyExperience(@PathVariable UUID uuid, @Valid @RequestBody UpdateExperienceRequest updateRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // Controller uses mapper to create a transient entity from the DTO.
        Experience updatedExperienceData = experienceMapper.toEntity(updateRequest);

        // Controller passes the transient entity, skills, and owner to the service.
        Experience updatedExperience = experienceService.updateExperience(uuid, updatedExperienceData, updateRequest.getSkills(), currentUser);

        // Map the persisted entity back to a DTO for the response.
        ExperienceDto updatedDto = experienceMapper.toDto(updatedExperience);
        return ResponseEntity.ok(new ApiResponseWrapper<>(updatedDto));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete one of my experiences")
    public ResponseEntity<Void> deleteMyExperience(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        experienceService.deleteExperience(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}