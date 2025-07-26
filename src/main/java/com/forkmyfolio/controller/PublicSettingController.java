package com.forkmyfolio.controller;

import com.forkmyfolio.dto.response.SettingDto;
import com.forkmyfolio.mapper.SettingMapper;
import com.forkmyfolio.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Endpoints for retrieving public settings")
@Slf4j
public class PublicSettingController {
    private final SettingService settingService;
    private final SettingMapper settingMapper; // Inject the mapper

    /**
     * --- THIS IS THE FIX ---
     * The method now returns a List of SettingDto objects.
     * This provides a rich, consistent data structure with uuid, name, and value
     * for every setting, which is exactly what the frontend needs.
     */
    @Operation(summary = "Get all public settings as a list of objects")
    @GetMapping
    public ResponseEntity<List<SettingDto>> getPublicSettings() {
        log.info("Request received for public settings list");
        var settings = settingService.getAllSettings();
        return ResponseEntity.ok(settingMapper.toDtoList(settings));
    }
}
