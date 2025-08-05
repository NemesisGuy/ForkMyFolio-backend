package com.forkmyfolio.controller;

import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.update.UpdateUserAccountRequest;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "User (Me)", description = "Endpoints for the authenticated user to manage their own profile.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')") // All endpoints in this controller require at least USER role
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "Get current user's profile", description = "Retrieves the detailed profile information for the currently authenticated user.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDto> getMyProfile() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UserDto userDto = userMapper.toDto(currentUser);
        // The ApiResponseWrapper will be applied automatically
        return ResponseEntity.ok(userDto);
    }

    @PutMapping
    @Operation(summary = "Update current user's profile", description = "Updates the first name, last name, and profile image URL for the currently authenticated user.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDto> updateMyProfile(@Valid @RequestBody UpdateUserAccountRequest request) {
        // The service now gets the user from the security context, so we don't need to pass an ID.
        User updatedUser = userService.updateUserProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getProfileImageUrl()
        );

        UserDto updatedDto = userMapper.toDto(updatedUser);
        return ResponseEntity.ok(updatedDto);
    }
}