package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.request.UpdateSettingRequest;
import com.forkmyfolio.dto.response.SettingDto;
import com.forkmyfolio.mapper.SettingMapper;
import com.forkmyfolio.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// Hidden Lines
@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin: Settings", description = "Endpoints for managing application settings")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminSettingController {

    private final SettingService settingService;
    private final SettingMapper settingMapper;

    @Operation(summary = "Get all settings")
    @GetMapping
    public ResponseEntity<List<SettingDto>> getAllSettings() {
        log.info("Admin request to get all settings");
        var settings = settingService.getAllSettings();
        return ResponseEntity.ok(settingMapper.toDtoList(settings));
    }

    @Operation(summary = "Update settings")
    @PutMapping
    public ResponseEntity<List<SettingDto>> updateSettings(@RequestBody @Valid List<UpdateSettingRequest> updateRequests) {
        log.info("Admin request to update {} settings", updateRequests.size());
        // 1. Convert the list of DTOs into a simple Map for the service layer.
        // This keeps the service layer clean and unaware of DTOs.
        Map<UUID, Boolean> settingsToUpdate = updateRequests.stream()
                .collect(Collectors.toMap(UpdateSettingRequest::getUuid, UpdateSettingRequest::getEnabled));

        // 2. Call the service to perform an efficient, transactional bulk update.
        var updatedSettings = settingService.updateSettings(settingsToUpdate);

        // 3. Map the updated entities back to DTOs for the response.
        return ResponseEntity.ok(settingMapper.toDtoList(updatedSettings));
    }
}