package com.forkmyfolio.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.mapper.BackupMapper;
import com.forkmyfolio.service.BackupService;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.model.PortfolioBackupData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for admin-only data backup and restore operations.
 *
 * @RequestMapping("/api/v1/admin")
 * @GetMapping("/backup") description = "Downloads a complete JSON backup of the admin's portfolio data, including profile, projects, skills, etc. This endpoint is secured for ADMIN users only.",      * @return A ResponseEntity containing the backup data and headers to trigger a file download.
 * @PostMapping("/ingest") description = "Restores the entire portfolio from a JSON backup file. THIS IS A DESTRUCTIVE OPERATION and will replace all existing portfolio data (projects, skills, etc.) for the user.",
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Operations", description = "Endpoints for administrative tasks like backups and statistics.")
@RequiredArgsConstructor
public class AdminBackupController {

    private final BackupService backupService;
    private final RestoreService restoreService; // New dependency
    private final BackupMapper backupMapper;
    private final ObjectMapper objectMapper; // New dependency for JSON parsing

    /**
     * Downloads a complete JSON backup of the admin's portfolio data.
     *
     * @return A ResponseEntity containing the backup data and headers to trigger a file download.
     */
    @GetMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download Portfolio Backup",
            description = "Downloads a complete JSON backup of the admin's portfolio data, including profile, projects, skills, etc. This endpoint is secured for ADMIN users only.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Backup file successfully generated and sent for download."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid."),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role.")
            })
    public ResponseEntity<PortfolioBackupDto> downloadBackup() {
        PortfolioBackupData backupData = backupService.createBackupForCurrentUser();
        PortfolioBackupDto backupDto = backupMapper.toDto(backupData);

        String userIdentifier = "portfolio";
        if (backupDto != null && backupDto.getProfile() != null) {
            String firstName = backupDto.getProfile().getFirstName();
            String lastName = backupDto.getProfile().getLastName();
            if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
                userIdentifier = (firstName + lastName).replaceAll("\\s+", "");
            }
        }
        String dateStamp = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String filename = String.format("%s-forkmyfolio-backup-%s.json", userIdentifier, dateStamp);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(backupDto);
    }

    /**
     * Restores the entire portfolio from a JSON backup file.
     * THIS IS A DESTRUCTIVE OPERATION and will replace all existing portfolio data.
     *
     * @param file The JSON backup file uploaded as multipart/form-data.
     * @return A 204 No Content response on success.
     * @throws IOException if the file cannot be read or parsed.
     */
    @PostMapping("/ingest")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ingest Portfolio Backup",
            description = "Restores the entire portfolio from a JSON backup file. THIS IS A DESTRUCTIVE OPERATION and will replace all existing portfolio data (projects, skills, etc.) for the user.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> ingestBackup(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Parse the file content into our DTO
        PortfolioBackupDto backupDto = objectMapper.readValue(file.getInputStream(), PortfolioBackupDto.class);

        // Call the service to perform the restore
        restoreService.restoreFromBackup(backupDto);

        // Return a success response
        return ResponseEntity.noContent().build();
    }
}