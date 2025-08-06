package com.forkmyfolio.controller.admin;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.create.AdminCreateUserRequest;
import com.forkmyfolio.dto.response.AdminStatsDto;
import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.dto.response.SettingDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.update.AdminUpdateUserRequest;
import com.forkmyfolio.dto.update.UpdateSettingRequest;
import com.forkmyfolio.mapper.SettingMapper;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ContactMessageService;
import com.forkmyfolio.service.SettingService;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.impl.VisitorStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Management", description = "Endpoints for administrative tasks.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final VisitorStatsService visitorStatsService;
    private final SettingService settingService;
    private final SettingMapper settingMapper;
    private final ContactMessageService contactMessageService;

    // --- User Management ---

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<User> userPage = userService.getAllUsers(pageable);
        Page<UserDto> userDtoPage = userPage.map(userMapper::toDto);
        return ResponseEntity.ok(userDtoPage);
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user (Admin)")
    public ResponseEntity<ApiResponseWrapper<UserDto>> createAdminUser(@Valid @RequestBody AdminCreateUserRequest request) {
        User newUser = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                null, // profileImageUrl can be null for admin creation
                request.getRoles(),
                request.getActive()
        );
        return new ResponseEntity<>(new ApiResponseWrapper<>(userMapper.toDto(newUser)), HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a single user by UUID")
    public ResponseEntity<ApiResponseWrapper<UserDto>> getUserByUuid(@PathVariable UUID userId) {
        User user = userService.getUserByUuid(userId);
        return ResponseEntity.ok(new ApiResponseWrapper<>(userMapper.toDto(user)));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update a user's details (Admin)")
    public ResponseEntity<ApiResponseWrapper<UserDto>> updateUserByAdmin(@PathVariable UUID userId, @Valid @RequestBody AdminUpdateUserRequest request) {
        User updatedUser = userService.updateUserByAdmin(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getSlug(),
                request.getRoles(),
                request.getActive()
        );
        return ResponseEntity.ok(new ApiResponseWrapper<>(userMapper.toDto(updatedUser)));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Deactivate a user (Soft Delete)")
    public ResponseEntity<ApiResponseWrapper<Map<String, String>>> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        Map<String, String> response = Map.of("message", "User deactivated successfully.");
        return ResponseEntity.ok(new ApiResponseWrapper<>(response));
    }

    // --- Stats Management ---
    @GetMapping("/stats")
    @Operation(summary = "Get all visitor statistics")
    public ResponseEntity<ApiResponseWrapper<AdminStatsDto>> getVisitorStats() {
        return ResponseEntity.ok(new ApiResponseWrapper<>(visitorStatsService.getStats()));
    }

    // --- Settings Management ---
    @GetMapping("/settings")
    @Operation(summary = "Get all application settings")
    public ResponseEntity<ApiResponseWrapper<List<SettingDto>>> getAllSettings() {
        var settings = settingService.getAllSettings();
        return ResponseEntity.ok(new ApiResponseWrapper<>(settingMapper.toDtoList(settings)));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update multiple application settings")
    public ResponseEntity<ApiResponseWrapper<List<SettingDto>>> updateSettings(@RequestBody @Valid List<UpdateSettingRequest> updateRequests) {
        Map<UUID, String> settingsToUpdate = updateRequests.stream()
                .collect(Collectors.toMap(UpdateSettingRequest::getUuid, UpdateSettingRequest::getValue));
        var updatedSettings = settingService.updateSettings(settingsToUpdate);
        return ResponseEntity.ok(new ApiResponseWrapper<>(settingMapper.toDtoList(updatedSettings)));
    }

    // --- Contact Message Management ---
    @GetMapping("/contact-messages")
    @Operation(summary = "Get all contact messages from all users")
    public ResponseEntity<ApiResponseWrapper<List<ContactMessageDto>>> getAllContactMessages() {
        List<ContactMessageDto> messages = contactMessageService.findAll();
        return ResponseEntity.ok(new ApiResponseWrapper<>(messages));
    }

    @DeleteMapping("/contact-messages/{uuid}")
    @Operation(summary = "Delete any contact message by its UUID")
    public ResponseEntity<ApiResponseWrapper<Map<String, String>>> deleteContactMessage(@PathVariable UUID uuid) {
        contactMessageService.deleteByUuid(uuid);
        Map<String, String> response = Map.of("message", "Message deleted successfully.");
        return ResponseEntity.ok(new ApiResponseWrapper<>(response));
    }
}