package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.create.CreatePortfolioProfileRequest;
import com.forkmyfolio.dto.response.PortfolioProfileDto;
import com.forkmyfolio.dto.update.UpdatePortfolioProfileRequest;
import com.forkmyfolio.mapper.PortfolioProfileMapper;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing the public-facing portfolio profile content.
 * This is a singleton resource, so it has C-R-U but no "list all" or "delete".
 */
@RestController
@RequestMapping("/api/v1/admin/portfolio-profile")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Portfolio Profile Management", description = "Endpoints for managing the public portfolio profile content.")
@SecurityRequirement(name = "bearerAuth")
public class AdminPortfolioProfileController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPortfolioProfileController.class);

    private final PortfolioProfileService portfolioProfileService;
    private final UserService userService;
    private final PortfolioProfileMapper portfolioProfileMapper;

    @Autowired
    public AdminPortfolioProfileController(PortfolioProfileService portfolioProfileService, UserService userService, PortfolioProfileMapper portfolioProfileMapper) {
        this.portfolioProfileService = portfolioProfileService;
        this.userService = userService;
        this.portfolioProfileMapper = portfolioProfileMapper;
    }

    /**
     * CREATE: Creates the portfolio profile for the first time.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create the portfolio profile", description = "Creates the public profile content for the first time. Fails if a profile already exists.")
    public PortfolioProfileDto createPortfolioProfile(@Valid @RequestBody CreatePortfolioProfileRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is creating their portfolio profile.", currentUser.getEmail());
        PortfolioProfile newProfile = portfolioProfileMapper.toEntity(request, currentUser);
        PortfolioProfile savedProfile = portfolioProfileService.createProfile(newProfile);
        return portfolioProfileMapper.toDto(savedProfile);
    }

    /**
     * READ: Retrieves the current portfolio profile for the admin to edit.
     */
    @GetMapping
    @Operation(summary = "Get portfolio profile content", description = "Retrieves the current public profile content for editing.")
    public PortfolioProfileDto getPortfolioProfile() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested their portfolio profile for editing.", currentUser.getEmail());
        PortfolioProfile profile = portfolioProfileService.getProfileByUser(currentUser);
        return portfolioProfileMapper.toDto(profile);
    }

    /**
     * UPDATE: Updates the entire state of the existing portfolio profile.
     * https://api-forkmyfolio.nemesisnet.co.za/api/v1/admin/portfolio-profile
     */
    @PutMapping
    @Operation(summary = "Update public portfolio profile", description = "Updates the main public profile information. Fails if a profile does not yet exist.")
    public PortfolioProfileDto updatePortfolioProfile(@Valid @RequestBody UpdatePortfolioProfileRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is updating their public portfolio profile.", currentUser.getEmail());
        PortfolioProfile existingProfile = portfolioProfileService.getProfileByUser(currentUser);
        portfolioProfileMapper.applyUpdateFromRequest(request, existingProfile);
        PortfolioProfile updatedProfile = portfolioProfileService.save(existingProfile);
        return portfolioProfileMapper.toDto(updatedProfile);
    }
}