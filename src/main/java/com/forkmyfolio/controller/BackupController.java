package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.aop.SkipApiResponseWrapper;
import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.mapper.BackupMapper;
import com.forkmyfolio.service.BackupService;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.model.PortfolioBackupData;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/v1/me/backup")
@Tag(name = "Backup & Restore (Me)", description = "Endpoints for the authenticated user to backup and restore their portfolio data.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class BackupController {

    private final BackupService backupService;
    private final RestoreService restoreService;
    private final BackupMapper backupMapper;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "Backup my entire portfolio", description = "Downloads a JSON file containing all of the authenticated user's portfolio data.")
    @SkipApiResponseWrapper
    public ResponseEntity<byte[]> downloadBackup() throws IOException {
        PortfolioBackupData backupData = backupService.createBackupForCurrentUser();
        PortfolioBackupDto backupDto = backupMapper.toDto(backupData);

        String filename = String.format("forkmyfolio-backup-%s.json", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        byte[] jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backupDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(jsonContent);
    }

    @PostMapping("/restore")
    @Operation(summary = "Restore my portfolio from a backup", description = "Upload a JSON backup file to restore portfolio data. This is a destructive action and will replace existing data.")
    public ResponseEntity<Void> restoreFromBackup(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(file.getContentType()))) {
            throw new IllegalArgumentException("Invalid file. Please upload a valid JSON backup file.");
        }

        PortfolioBackupDto backupDto = objectMapper.readValue(file.getInputStream(), PortfolioBackupDto.class);
        restoreService.restoreFromBackup(backupDto);

        return ResponseEntity.noContent().build();
    }
}