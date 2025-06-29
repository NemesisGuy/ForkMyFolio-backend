package com.forkmyfolio.controller;

import com.forkmyfolio.dto.QualificationDto;
import com.forkmyfolio.mapper.QualificationMapper;
import com.forkmyfolio.service.QualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for PUBLICLY viewing portfolio qualifications.
 */
@RestController
@RequestMapping("/api/v1/qualifications")
@Tag(name = "Qualifications", description = "Public endpoint for viewing portfolio qualifications.")
public class QualificationController {

    private static final Logger logger = LoggerFactory.getLogger(QualificationController.class);
    private final QualificationService qualificationService;
    private final QualificationMapper qualificationMapper;

    @Autowired
    public QualificationController(QualificationService qualificationService, QualificationMapper qualificationMapper) {
        this.qualificationService = qualificationService;
        this.qualificationMapper = qualificationMapper;
    }

    /**
     * Retrieves the list of public qualifications for the portfolio.
     *
     * @return A list of {@link QualificationDto}.
     */
    @GetMapping
    @Operation(summary = "Get all public qualifications", description = "Retrieves a list of all qualifications (e.g., degrees, certifications) for the portfolio.")
    public List<QualificationDto> getPublicQualifications() {
        logger.info("Request received for public qualifications.");

        // 1. Call the service to get the list of Qualification entities.
        List<QualificationDto> qualifications = qualificationService.getPublicQualifications().stream()
                // 2. Use the mapper to convert each entity to its DTO representation.
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList());

        logger.info("Successfully retrieved {} public qualifications.", qualifications.size());
        return qualifications;
    }
}