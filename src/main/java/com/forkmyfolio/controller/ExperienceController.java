package com.forkmyfolio.controller;

import com.forkmyfolio.dto.response.ExperienceDto;
import com.forkmyfolio.mapper.ExperienceMapper;
import com.forkmyfolio.service.ExperienceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
// ... imports

@RestController
@RequestMapping("/api/v1/experience")
public class ExperienceController {
    private final ExperienceService experienceService;
    private final ExperienceMapper experienceMapper;

    private static final Logger logger = LoggerFactory.getLogger(ExperienceController.class);

    public ExperienceController(ExperienceService experienceService, ExperienceMapper experienceMapper) {
        this.experienceService = experienceService;
        this.experienceMapper = experienceMapper;
    }

//add logs
    @GetMapping
    public List<ExperienceDto> getPublicExperience() {
         logger.info("Fetching all public experiences.");

        List<ExperienceDto> experiences = experienceService.getPublicExperience().stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList());
        logger.info("Successfully fetched {} public experiences.", experiences.size());
        return experiences;



    }
}