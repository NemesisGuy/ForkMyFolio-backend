package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.AdminStatsDto;
import com.forkmyfolio.service.VisitorStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {
    private final VisitorStatsService visitorStatsService;

    @GetMapping
    public ResponseEntity<AdminStatsDto> getVisitorStats() {
        return ResponseEntity.ok(visitorStatsService.getStats());
    }
}