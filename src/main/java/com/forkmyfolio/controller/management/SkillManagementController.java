package com.forkmyfolio.controller.management;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.dto.update.UpdateSkillRequest;
import com.forkmyfolio.mapper.SkillMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.service.SkillService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/me/skills")
@Tag(name = "Skill Management (Me)", description = "Endpoints for the authenticated user to manage their skill portfolio.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class SkillManagementController {

    private final SkillService skillService;
    private final UserService userService;
    private final SkillMapper skillMapper;

    @GetMapping
    @Operation(summary = "Get all of my skills")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseWrapper<List<SkillDto>>> getMySkills() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<UserSkill> userSkills = skillService.getAllSkillsForUser(currentUser);

        log.info("Found {} skills for user {}. Mapping to detailed DTOs.", userSkills.size(), currentUser.getUuid());

        List<SkillDto> skillDtos = userSkills.stream()
                .map(userSkill -> {
                    // This explicit mapping is more robust and helps in debugging.
                    if (userSkill == null || userSkill.getSkill() == null) {
                        log.warn("A null UserSkill or its associated Skill was found for user {}", currentUser.getUuid());
                        return null;
                    }
                    // The toDetailDto method is designed to include user-specific fields like level, visibility, etc.
                    return skillMapper.toDetailDto(userSkill);
                })
                .filter(Objects::nonNull) // Ensure no null DTOs are included in the final list.
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponseWrapper<>(skillDtos));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get one of my skills by its UserSkill UUID")
    public ResponseEntity<ApiResponseWrapper<SkillDto>> getMySkillByUuid(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UserSkill userSkill = skillService.getSkillForUser(currentUser, uuid);
        // Using toDetailDto ensures all user-specific fields are included in the response.
        SkillDto skillDto = skillMapper.toDetailDto(userSkill);
        return ResponseEntity.ok(new ApiResponseWrapper<>(skillDto));
    }

    @PostMapping
    @Operation(summary = "Add a new skill to my portfolio")
    public ResponseEntity<ApiResponseWrapper<SkillDto>> addSkillToMyPortfolio(@Valid @RequestBody CreateSkillRequest createRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UserSkill newUserSkill = skillService.addSkillToUser(createRequest, currentUser);
        SkillDto skillDto = skillMapper.toDetailDto(newUserSkill);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseWrapper<>(skillDto));
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update my relationship with a skill (e.g., proficiency level)")
    public ResponseEntity<ApiResponseWrapper<SkillDto>> updateMySkill(@PathVariable UUID uuid, @Valid @RequestBody UpdateSkillRequest updateRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UserSkill updatedUserSkill = skillService.updateSkillForUser(uuid, updateRequest, currentUser);
        SkillDto skillDto = skillMapper.toDetailDto(updatedUserSkill);
        return ResponseEntity.ok(new ApiResponseWrapper<>(skillDto));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Remove a skill from my portfolio")
    public ResponseEntity<Void> removeSkillFromMyPortfolio(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        skillService.removeSkillFromUser(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}