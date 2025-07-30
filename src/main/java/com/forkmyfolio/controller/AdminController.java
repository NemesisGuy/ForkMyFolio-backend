package com.forkmyfolio.controller;

import com.forkmyfolio.dto.create.AdminCreateUserRequest;
import com.forkmyfolio.dto.response.AdminStatsDto;
import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.dto.response.SettingDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.update.AdminUpdateUserRequest;
import com.forkmyfolio.dto.update.UpdateSettingRequest;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.mapper.SettingMapper;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.BackupRestoreService;
import com.forkmyfolio.service.ContactMessageService;
import com.forkmyfolio.service.SettingService;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.impl.VisitorStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ContactMessageMapper contactMessageMapper;
    private final BackupRestoreService backupRestoreService;

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
    public ResponseEntity<UserDto> createAdminUser(@Valid @RequestBody AdminCreateUserRequest request) {
        User newUser = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                null, // profileImageUrl can be null for admin creation
                request.getRoles(),
                request.getActive()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(newUser));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a single user by UUID")
    public ResponseEntity<UserDto> getUserByUuid(@PathVariable UUID userId) {
        User user = userService.getUserByUuid(userId);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update a user's details (Admin)")
    public ResponseEntity<UserDto> updateUserByAdmin(@PathVariable UUID userId, @Valid @RequestBody AdminUpdateUserRequest request) {
        User updatedUser = userService.updateUserByAdmin(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getSlug(),
                request.getRoles(),
                request.getActive()
        );
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Deactivate a user (Soft Delete)")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    // --- Stats Management ---
    @GetMapping("/stats")
    @Operation(summary = "Get all visitor statistics")
    public ResponseEntity<AdminStatsDto> getVisitorStats() {
        return ResponseEntity.ok(visitorStatsService.getStats());
    }

    // --- Settings Management ---
    @GetMapping("/settings")
    @Operation(summary = "Get all application settings")
    public ResponseEntity<List<SettingDto>> getAllSettings() {
        var settings = settingService.getAllSettings();
        return ResponseEntity.ok(settingMapper.toDtoList(settings));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update multiple application settings")
    public ResponseEntity<List<SettingDto>> updateSettings(@RequestBody @Valid List<UpdateSettingRequest> updateRequests) {
        Map<UUID, String> settingsToUpdate = updateRequests.stream()
                .collect(Collectors.toMap(UpdateSettingRequest::getUuid, UpdateSettingRequest::getValue));
        var updatedSettings = settingService.updateSettings(settingsToUpdate);
        return ResponseEntity.ok(settingMapper.toDtoList(updatedSettings));
    }

    // --- Contact Message Management ---
    @GetMapping("/contact-messages")
    @Operation(summary = "Get all contact messages from all users")
    public ResponseEntity<List<ContactMessageDto>> getAllContactMessages() {
        var messages = contactMessageService.findAll();
        return ResponseEntity.ok(contactMessageMapper.toDtoList(messages));
    }

    @DeleteMapping("/contact-messages/{uuid}")
    @Operation(summary = "Delete any contact message by its UUID")
    public ResponseEntity<Void> deleteContactMessage(@PathVariable UUID uuid) {
        contactMessageService.deleteByUuid(uuid);
        return ResponseEntity.noContent().build();
    }

    // --- Backup & Restore Management ---
    @GetMapping("/backup/system")
    @Operation(summary = "Generate and download a full system backup",
            description = "Creates a JSON file containing all users and their complete portfolio data (projects, skills, experience, etc.).")
    public ResponseEntity<InputStreamResource> backupSystem() {
        String jsonData = backupRestoreService.generateSystemBackupJson();
        byte[] backupBytes = jsonData.getBytes();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm"));
        String filename = "forkmyfolio-backup-" + timestamp + ".json";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(backupBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new ByteArrayInputStream(backupBytes)));
    }

    @PostMapping("/restore/system")
    @Operation(summary = "Restore the system from a backup file", description = "WARNING: This is a destructive operation. It will wipe existing data and replace it with the data from the backup file.")
    public ResponseEntity<Void> restoreSystem(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        backupRestoreService.restoreSystemFromJson(file.getInputStream());
        return ResponseEntity.noContent().build();
    }
}