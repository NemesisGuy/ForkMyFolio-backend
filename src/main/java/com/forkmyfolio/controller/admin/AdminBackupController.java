package com.forkmyfolio.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.aop.SkipApiResponseWrapper;
import com.forkmyfolio.dto.backup.BackupFileDto;
import com.forkmyfolio.dto.backup.BackupMetaDto;
import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.BackupService;
import com.forkmyfolio.service.BackupValidationService;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/backup")
@Tag(name = "Admin: System Backup & Restore", description = "Endpoints for administrators to perform system-wide backups and restores.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AdminBackupController {

    // Services for core logic
    private final BackupValidationService backupValidationService;
    private final RestoreService restoreService;
    private final UserService userService;
    private final BackupService backupService;

    // Mappers for DTO conversion
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @GetMapping
    @Operation(summary = "Create a full system backup", description = "Downloads a versioned JSON file containing all data for all users in the system.")
    @SkipApiResponseWrapper
    public ResponseEntity<byte[]> downloadSystemBackup() throws IOException {
        log.info("Admin request for system-wide backup initiated.");
        List<User> allUsers = userService.getAllUsersWithPortfolioData();
        List<UserFullBackupDto> systemBackupData = new ArrayList<>();

        for (User user : allUsers) {
            PortfolioBackupDto portfolioBackup = backupService.createBackupDtoForUser(user);
            UserDto userDto = userMapper.toDto(user);
            systemBackupData.add(new UserFullBackupDto(userDto, portfolioBackup));
        }

        log.info("Successfully generated backup data for {} users.", systemBackupData.size());

        BackupMetaDto meta = BackupMetaDto.builder()
                .version(appVersion)
                .exportedAt(ZonedDateTime.now())
                .type("system_backup")
                .compatibility(BackupMetaDto.Compatibility.builder()
                        .minSupportedVersion("2.0.0")
                        .maxSupportedVersion("2.x")
                        .build())
                .system("ForkMyFolio")
                .build();

        BackupFileDto<List<UserFullBackupDto>> backupFile = new BackupFileDto<>(meta, systemBackupData);

        String filename = String.format("forkmyfolio-system-backup-%s.json", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        byte[] jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backupFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok().headers(headers).body(jsonContent);
    }

    @PostMapping("/restore/system")
    @Operation(summary = "Restore the entire system from a backup", description = "Upload a system backup file. THIS IS A DESTRUCTIVE OPERATION and will wipe all existing data before restoring.")
    public ResponseEntity<Void> restoreSystemFromBackup(@RequestParam("file") MultipartFile file) throws IOException {
        log.warn("Admin request for SYSTEM-WIDE restore initiated. THIS IS A DESTRUCTIVE OPERATION.");
        if (file.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(file.getContentType()))) {
            throw new IllegalArgumentException("Invalid file. Please upload a valid JSON backup file.");
        }

        BackupFileDto<List<UserFullBackupDto>> backupFile = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {});

        backupValidationService.validateBackup(backupFile.getMeta(), "system_backup");
        restoreService.restoreSystemFromBackup(backupFile.getData());

        log.warn("System restore completed successfully. {} users restored.", backupFile.getData().size());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/user/{userUuid}")
    @Operation(summary = "Restore a single user's portfolio from a backup", description = "Upload a standard user backup file to restore a specific user's portfolio. This is a destructive action for the target user only.")
    public ResponseEntity<Void> restoreSingleUser(
            @Parameter(description = "The UUID of the user to restore.") @PathVariable UUID userUuid,
            @RequestParam("file") MultipartFile file) throws IOException {

        log.warn("Admin request to restore a single user's portfolio for UUID: {}", userUuid);
        if (file.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(file.getContentType()))) {
            throw new IllegalArgumentException("Invalid file. Please upload a valid JSON backup file.");
        }

        User targetUser = userService.getUserByUuid(userUuid);
        BackupFileDto<PortfolioBackupDto> backupFile = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {});

        backupValidationService.validateBackup(backupFile.getMeta(), "user_backup");
        restoreService.restoreUserFromBackup(targetUser, backupFile.getData());

        log.info("Successfully restored portfolio for user: {}", targetUser.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wipe")
    @Operation(summary = "Wipe all data from the system", description = "THIS IS A HIGHLY DESTRUCTIVE OPERATION. It deletes all users, portfolios, skills, and other associated data from the database. Use with extreme caution.")
    public ResponseEntity<Void> wipeSystemData() {
        log.warn("ADMIN-INITIATED DATA WIPE. ALL DATA WILL BE DELETED.");
        restoreService.wipeAllData();
        log.warn("System data wipe completed successfully.");
        return ResponseEntity.noContent().build();
    }
}