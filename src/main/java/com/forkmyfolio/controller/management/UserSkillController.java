package com.forkmyfolio.controller.management;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.request.CreateUserSkillDto;
import com.forkmyfolio.dto.response.UserSkillResponseDto;
import com.forkmyfolio.dto.update.UpdateUserSkillRequest;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/skills")
@RequiredArgsConstructor
@Tag(name = "User Skills", description = "Endpoints for managing the authenticated user's skills.")
@PreAuthorize("isAuthenticated()")
public class UserSkillController {

    private final UserSkillService userSkillService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all skills for the current user", description = "Retrieves a list of all skills associated with the authenticated user.")
    public ResponseEntity<ApiResponseWrapper<List<UserSkillResponseDto>>> getAllUserSkills() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<UserSkillResponseDto> skills = userSkillService.getAllUserSkills(currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper<>(skills));
    }

    @PostMapping
    @Operation(summary = "Add a new skill to the current user's profile", description = "Creates a new skill relationship for the authenticated user. If the skill doesn't exist globally, it will be created.")
    public ResponseEntity<ApiResponseWrapper<UserSkillResponseDto>> createUserSkill(@Valid @RequestBody CreateUserSkillDto createDto) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UserSkillResponseDto newSkill = userSkillService.createUserSkill(createDto, currentUser);
        return new ResponseEntity<>(new ApiResponseWrapper<>(newSkill), HttpStatus.CREATED);
    }

    @PutMapping("/{userSkillId}")
    @Operation(summary = "Update a user's skill", description = "Updates details of a specific skill relationship for the authenticated user, such as proficiency level or visibility.")
    public ResponseEntity<ApiResponseWrapper<UserSkillResponseDto>> updateUserSkill(
            @PathVariable UUID userSkillId,
            @Valid @RequestBody UpdateUserSkillRequest updateDto) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UserSkillResponseDto updatedSkill = userSkillService.updateUserSkill(userSkillId, updateDto, currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper<>(updatedSkill));
    }

    @DeleteMapping("/{userSkillId}")
    @Operation(summary = "Remove a skill from the current user's profile", description = "Deletes a skill relationship for the authenticated user.")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteUserSkill(@PathVariable UUID userSkillId) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        userSkillService.deleteUserSkill(userSkillId, currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper<>(null, "Skill removed successfully."));
    }
}