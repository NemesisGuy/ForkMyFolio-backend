package com.forkmyfolio.controller;

import com.forkmyfolio.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Endpoints for retrieving public settings")
@Slf4j
public class PublicSettingController {
    private final SettingService settingService;

    @Operation(summary = "Get all public settings as a map")
    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getPublicSettings() {
        log.info("Request received for public settings map");
        return ResponseEntity.ok(settingService.getPublicSettings());
    }
}