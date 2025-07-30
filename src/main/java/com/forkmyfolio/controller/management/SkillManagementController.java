package com.forkmyfolio.controller.management;

import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.dto.update.UpdateSkillRequest;
import com.forkmyfolio.mapper.SkillMapper;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.SkillService;
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
@RequestMapping("/api/v1/me/skills")
@Tag(name = "Skill Management (Me)", description = "Endpoints for the authenticated user to manage their own skills.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class SkillManagementController {

    private final SkillService skillService;
    private final UserService userService;
    private final SkillMapper skillMapper;

    @GetMapping
    @Operation(summary = "Get all of my skills")
    public ResponseEntity<List<SkillDto>> getMySkills() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Skill> skills = skillService.getSkillsForUser(currentUser);
        List<SkillDto> skillDtos = skills.stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(skillDtos);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get one of my skills by its UUID")
    public ResponseEntity<SkillDto> getMySkillByUuid(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Skill skill = skillService.findSkillByUuidAndUser(uuid, currentUser);
        return ResponseEntity.ok(skillMapper.toDto(skill));
    }

    @PostMapping
    @Operation(summary = "Create a new skill for myself")
    public ResponseEntity<SkillDto> createMySkill(@Valid @RequestBody CreateSkillRequest createRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Skill newSkill = skillMapper.toEntity(createRequest, currentUser);
        Skill createdSkill = skillService.createSkill(newSkill);
        return new ResponseEntity<>(skillMapper.toDto(createdSkill), HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update one of my skills")
    public ResponseEntity<SkillDto> updateMySkill(@PathVariable UUID uuid, @Valid @RequestBody UpdateSkillRequest updateRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        // The service layer handles fetching, ownership checks, and the update logic.
        // This keeps the controller thin and focused on HTTP concerns.
        Skill updatedSkill = skillService.updateSkill(
                uuid,
                updateRequest.getName(),
                updateRequest.getLevel(),
                updateRequest.getVisible(),
                updateRequest.getCategory(),
                updateRequest.getIcon(),
                updateRequest.getDescription(),
                currentUser
        );
        return ResponseEntity.ok(skillMapper.toDto(updatedSkill));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete one of my skills")
    public ResponseEntity<Void> deleteMySkill(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        skillService.deleteSkill(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}