package com.forkmyfolio.controller;

import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for PUBLICLY viewing portfolio skills.
 */
@RestController
@RequestMapping("/api/v1/skills")
@Tag(name = "Skills", description = "Public endpoints for viewing portfolio skills")
public class SkillController {

    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
    private final SkillService skillService;

    @Autowired
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    /**
     * Retrieves all public skills for the portfolio owner.
     */
    @GetMapping
    @Operation(summary = "Get all public skills", description = "Retrieves a list of all skills for the portfolio.")
    public List<SkillDto> getPublicSkills() {
        logger.info("Received request to get all public skills.");
        // This service method will need to be implemented to get the owner's skills
        List<SkillDto> skills = skillService.getPublicSkills();
        logger.info("Successfully retrieved {} public skills.", skills.size());
        return skills;
    }

    /**
     * Retrieves a specific public skill by its ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a public skill by ID", description = "Retrieves a specific skill by its ID.")
    public SkillDto getSkillById(@Parameter(description = "ID of the skill to be retrieved") @PathVariable Long id) {
        logger.info("Received request to get public skill by ID: {}", id);
        SkillDto skill = skillService.getSkillById(id);
        logger.info("Successfully retrieved public skill with ID: {}", id);
        return skill;
    }
}