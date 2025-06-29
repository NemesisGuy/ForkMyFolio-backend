package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.CreateQualificationRequest;
import com.forkmyfolio.dto.QualificationDto;
import com.forkmyfolio.dto.UpdateQualificationRequest;
import com.forkmyfolio.mapper.QualificationMapper;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.QualificationService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/qualifications")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Qualification Management", description = "Endpoints for managing qualifications.")
@SecurityRequirement(name = "bearerAuth")
public class AdminQualificationController {

    private static final Logger logger = LoggerFactory.getLogger(AdminQualificationController.class);

    private final QualificationService qualificationService;
    private final UserService userService;
    private final QualificationMapper qualificationMapper;

    @Autowired
    public AdminQualificationController(QualificationService qualificationService, UserService userService, QualificationMapper qualificationMapper) {
        this.qualificationService = qualificationService;
        this.userService = userService;
        this.qualificationMapper = qualificationMapper;
    }

    // --- READ (List All) ---
    @GetMapping
    @Operation(summary = "List all qualifications", description = "Retrieves a list of all qualification entries for the admin to manage.")
    public List<QualificationDto> getAllQualificationsForAdmin() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested all qualification entries.", currentUser.getEmail());
        List<Qualification> qualifications = qualificationService.getPublicQualifications();
        return qualifications.stream()
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- READ (Single by UUID) ---
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a single qualification by UUID", description = "Retrieves a single qualification by its UUID for editing.")
    public QualificationDto getQualificationByUuid(@Parameter(description = "The UUID of the qualification") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested qualification with UUID: {}", currentUser.getEmail(), uuid);
        Qualification qualification = qualificationService.getQualificationByUuid(uuid);
        return qualificationMapper.toDto(qualification);
    }

    // --- CREATE ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new qualification")
    public QualificationDto createQualification(@Valid @RequestBody CreateQualificationRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new qualification: {}", currentUser.getEmail(), request.getQualificationName());
        Qualification newQualification = qualificationMapper.toEntity(request, currentUser);
        Qualification savedQualification = qualificationService.createQualification(newQualification);
        return qualificationMapper.toDto(savedQualification);
    }

    // --- UPDATE ---
    @PutMapping("/{uuid}")
    @Operation(summary = "Update a qualification by UUID")
    public QualificationDto updateQualification(@Parameter(description = "The UUID of the qualification") @PathVariable UUID uuid, @Valid @RequestBody UpdateQualificationRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating qualification UUID: {}", currentUser.getEmail(), uuid);
        Qualification existingQualification = qualificationService.getQualificationByUuid(uuid);

        // Authorization check
        if (!existingQualification.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to update this qualification.");
        }

        qualificationMapper.applyUpdateFromRequest(request, existingQualification);
        Qualification updatedQualification = qualificationService.save(existingQualification);
        return qualificationMapper.toDto(updatedQualification);
    }

    // --- DELETE ---
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete a qualification by UUID")
    public ResponseEntity<Void> deleteQualification(@Parameter(description = "The UUID of the qualification") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting qualification UUID: {}", currentUser.getEmail(), uuid);
        qualificationService.deleteQualification(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}