package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.mapper.SkillMapper; // <-- 1. IMPORT MAPPER
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.service.VisitorStatsService;
import com.forkmyfolio.service.SkillService;
import com.forkmyfolio.util.SecurityUtils;
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
import java.util.UUID; // <-- 2. IMPORT UUID
import java.util.stream.Collectors;

/**
 * Controller for PUBLICLY viewing portfolio skills.
 */
@RestController
@RequestMapping("/api/v1/skills")
@Tag(name = "Skills", description = "Public endpoints for viewing portfolio skills")
public class SkillController {

    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
    private final SkillService skillService;
    private final SkillMapper skillMapper; // <-- 3. INJECT MAPPER
    private final VisitorStatsService visitorStatsService;
    private final SecurityUtils securityUtils;

    @Autowired
    public SkillController(SkillService skillService, SkillMapper skillMapper, VisitorStatsService visitorStatsService, SecurityUtils securityUtils) { // <-- 4. UPDATE CONSTRUCTOR
        this.skillService = skillService;
        this.skillMapper = skillMapper;
        this.visitorStatsService = visitorStatsService;
        this.securityUtils = securityUtils;
    }

    /**
     * Retrieves all public skills for the portfolio owner.
     */
    @GetMapping
    @Operation(summary = "Get all public skills", description = "Retrieves a list of all skills for the portfolio.")
    @TrackVisitor(VisitorStatType.SKILLS_SECTION_VIEW)
    public List<SkillDto> getPublicSkills() {
        logger.info("Received request to get all public skills.");

        // 5. FIX: Call the DTO-less service method and use the mapper
        List<Skill> skillEntities = skillService.getPublicSkills();
        List<SkillDto> skillDtos = skillEntities.stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());

        logger.info("Successfully retrieved {} public skills.", skillDtos.size());
        return skillDtos;
    }

    /**
     * Retrieves a specific public skill by its UUID.
     */
    // 6. FIX: Change endpoint to use UUID
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a public skill by its UUID", description = "Retrieves a specific skill by its public UUID.")
    public SkillDto getSkillByUuid(@Parameter(description = "UUID of the skill to be retrieved") @PathVariable UUID uuid) {
        logger.info("Received request to get public skill by UUID: {}", uuid);

        // 7. FIX: Call the correct UUID-based service method
        Skill skillEntity = skillService.getSkillByUuid(uuid);
        logger.info("Successfully retrieved public skill with UUID: {}", uuid);

        // 8. FIX: Use the mapper for the response
        return skillMapper.toDto(skillEntity);
    }
}