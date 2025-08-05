package com.forkmyfolio.controller.management;

import com.forkmyfolio.dto.create.CreateExperienceRequest;
import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.dto.update.UpdateExperienceRequest;
import com.forkmyfolio.mapper.ExperienceMapper;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
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
    public ResponseEntity<List<ExperienceDto>> getMyExperiences() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Experience> experiences = experienceService.getExperiencesForUser(currentUser);
        List<ExperienceDto> experienceDtos = experiences.stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(experienceDtos);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get one of my experiences by its UUID")
    public ResponseEntity<ExperienceDto> getMyExperienceByUuid(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Experience experience = experienceService.findExperienceByUuidAndUser(uuid, currentUser);
        return ResponseEntity.ok(experienceMapper.toDto(experience));
    }

    @PostMapping
    @Operation(summary = "Create a new experience for myself")
    public ResponseEntity<ExperienceDto> createMyExperience(@Valid @RequestBody CreateExperienceRequest createRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Experience newExperience = experienceMapper.toEntity(createRequest, currentUser);
        Experience createdExperience = experienceService.createExperience(newExperience, createRequest.getSkills());
        return new ResponseEntity<>(experienceMapper.toDto(createdExperience), HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update one of my experiences")
    public ResponseEntity<ExperienceDto> updateMyExperience(@PathVariable UUID uuid, @Valid @RequestBody UpdateExperienceRequest updateRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Experience updatedExperienceData = experienceMapper.toEntity(updateRequest);
        Experience updatedExperience = experienceService.updateExperience(uuid, updatedExperienceData, updateRequest.getSkills(), currentUser);
        return ResponseEntity.ok(experienceMapper.toDto(updatedExperience));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete one of my experiences")
    public ResponseEntity<Void> deleteMyExperience(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        experienceService.deleteExperience(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}