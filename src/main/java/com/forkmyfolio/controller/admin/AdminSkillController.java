package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.CreateSkillRequest;
import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.dto.UpdateSkillRequest; // Import the new DTO
import com.forkmyfolio.mapper.SkillMapper;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/skills")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Skill Management", description = "Endpoints for managing portfolio skills.")
@SecurityRequirement(name = "bearerAuth")
public class AdminSkillController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSkillController.class);

    private final SkillService skillService;
    private final UserService userService;
    private final SkillMapper skillMapper;

    @Autowired
    public AdminSkillController(SkillService skillService, UserService userService, SkillMapper skillMapper) {
        this.skillService = skillService;
        this.userService = userService;
        this.skillMapper = skillMapper;
    }

    // --- READ (List All) ---
    @GetMapping
    @Operation(summary = "List all skills", description = "Retrieves a list of all skill entries for the admin to manage.")
    public List<SkillDto> getAllSkillsForAdmin() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested all skill entries.", currentUser.getEmail());
        List<Skill> skills = skillService.getPublicSkills();
        return skills.stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- READ (Single by UUID) ---
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a single skill by UUID", description = "Retrieves a single skill by its UUID for editing.")
    public SkillDto getSkillByUuid(@Parameter(description = "The UUID of the skill") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested skill with UUID: {}", currentUser.getEmail(), uuid);
        Skill skill = skillService.getSkillByUuid(uuid);
        return skillMapper.toDto(skill);
    }

    // --- CREATE ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new skill")
    public SkillDto createSkill(@Valid @RequestBody CreateSkillRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new skill: {}", currentUser.getEmail(), request.getName());
        Skill newSkill = skillMapper.toEntity(request, currentUser);
        Skill savedSkill = skillService.createSkill(newSkill);
        return skillMapper.toDto(savedSkill);
    }

    // --- UPDATE ---
    @PutMapping("/{uuid}")
    @Operation(summary = "Update a skill's proficiency level by UUID")
    public SkillDto updateSkill(@Parameter(description = "The UUID of the skill to update") @PathVariable UUID uuid, @Valid @RequestBody UpdateSkillRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating skill UUID: {}", currentUser.getEmail(), uuid);
        Skill existingSkill = skillService.getSkillByUuid(uuid);

        // Use a transient entity to pass updated details to the service
        Skill updatedDetails = new Skill();
        updatedDetails.setLevel(request.getLevel());

        Skill updatedSkill = skillService.updateSkill(uuid, updatedDetails, currentUser);
        return skillMapper.toDto(updatedSkill);
    }

    // --- DELETE ---
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete a skill by UUID")
    public ResponseEntity<Void> deleteSkill(@Parameter(description = "The UUID of the skill") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting skill UUID: {}", currentUser.getEmail(), uuid);
        skillService.deleteSkill(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}