package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.mapper.ExperienceMapper;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.ExperienceService;
import com.forkmyfolio.service.VisitorStatsService;
import com.forkmyfolio.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
// ... imports

@RestController
@RequestMapping("/api/v1/experience")
public class ExperienceController {
    private static final Logger logger = LoggerFactory.getLogger(ExperienceController.class);
    private final ExperienceService experienceService;
    private final VisitorStatsService visitorStatsService;
    private final ExperienceMapper experienceMapper;
    private final SecurityUtils securityUtils;

    public ExperienceController(ExperienceService experienceService, VisitorStatsService visitorStatsService, ExperienceMapper experienceMapper, SecurityUtils securityUtils) {
        this.experienceService = experienceService;
        this.visitorStatsService = visitorStatsService;
        this.experienceMapper = experienceMapper;
        this.securityUtils = securityUtils;
    }

    //add logs
    @GetMapping
    @TrackVisitor(VisitorStatType.EXPERIENCE_SECTION_VIEW)
    public List<ExperienceDto> getPublicExperience() {
        logger.info("Fetching all public experiences.");

        List<ExperienceDto> experiences = experienceService.getPublicExperience().stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList());
        logger.info("Successfully fetched {} public experiences.", experiences.size());
        return experiences;


    }
}