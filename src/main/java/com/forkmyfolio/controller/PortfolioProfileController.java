package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.PortfolioProfileDto;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.mapper.PortfolioProfileMapper;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.VisitorStatsService;
import com.forkmyfolio.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the public-facing portfolio profile.
 */
@RestController
@RequestMapping("/api/v1/portfolio-profile") // <-- RENAMED
@Tag(name = "Public Portfolio Profile", description = "Endpoint for viewing the portfolio owner's public profile.") // <-- RENAMED
public class PortfolioProfileController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioProfileController.class);

    private final PortfolioProfileService portfolioProfileService;
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final VisitorStatsService visitorStatsService;
    private final SecurityUtils securityUtils;

    @Autowired
    public PortfolioProfileController(PortfolioProfileService portfolioProfileService, PortfolioProfileMapper portfolioProfileMapper, VisitorStatsService visitorStatsService, SecurityUtils securityUtils) {
        this.portfolioProfileService = portfolioProfileService;
        this.portfolioProfileMapper = portfolioProfileMapper;
        this.visitorStatsService = visitorStatsService;
        this.securityUtils = securityUtils;
    }

    /**
     * Retrieves the public profile of the portfolio owner.
     *
     * @return The PortfolioProfileDto of the portfolio owner.
     */
    @GetMapping
    @Operation(summary = "Get the public portfolio profile", description = "Retrieves the main profile information for the portfolio owner.")
    @TrackVisitor(VisitorStatType.TOTAL_VISITS)
    public PortfolioProfileDto getPublicProfile() {
        logger.info("Request received for public portfolio profile.");
        PortfolioProfile portfolioProfile = portfolioProfileService.getPublicProfile();

        logger.info("Successfully retrieved public portfolio profile.");
        return portfolioProfileMapper.toDto(portfolioProfile);
    }
}