package com.forkmyfolio.controller.management;

import com.forkmyfolio.dto.response.PortfolioProfileDto;
import com.forkmyfolio.dto.update.UpdatePortfolioProfileRequest;
import com.forkmyfolio.dto.update.UpdateProfileVisibilityRequest;
import com.forkmyfolio.mapper.PortfolioProfileMapper;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/profile")
@Tag(name = "Portfolio Profile Management (Me)", description = "Endpoints for the authenticated user to manage their own detailed portfolio profile.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioProfileManagementController {

    private final UserService userService;
    private final PortfolioProfileService portfolioProfileService;
    private final PortfolioProfileMapper portfolioProfileMapper;

    @GetMapping
    @Operation(summary = "Get my portfolio profile", description = "Retrieves the detailed portfolio profile (headline, summary, links, etc.) for the currently authenticated user.")
    public ResponseEntity<PortfolioProfileDto> getMyPortfolioProfile() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        PortfolioProfile profile = portfolioProfileService.getProfileByUser(currentUser);
        PortfolioProfileDto dto = portfolioProfileMapper.toDto(profile);

        // FIX: Explicitly set the public status to ensure it matches the entity's true state,
        // overriding any potential inconsistencies in the mapping layer.
        dto.setPublic(profile.isPublic());

        return ResponseEntity.ok(dto);
    }

    @PutMapping
    @Operation(summary = "Update my portfolio profile", description = "Updates the detailed portfolio profile for the currently authenticated user. All fields are optional.")
    public ResponseEntity<PortfolioProfileDto> updateMyPortfolioProfile(@Valid @RequestBody UpdatePortfolioProfileRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        PortfolioProfile profileToUpdate = portfolioProfileService.getProfileByUser(currentUser);

        // The mapper applies the changes from the DTO to the entity
        portfolioProfileMapper.applyUpdateFromRequest(request, profileToUpdate);

        // The service saves the updated entity
        PortfolioProfile updatedProfile = portfolioProfileService.save(profileToUpdate);

        PortfolioProfileDto responseDto = portfolioProfileMapper.toDto(updatedProfile);
        // FIX: Ensure the returned DTO is consistent with the updated entity's state.
        responseDto.setPublic(updatedProfile.isPublic());

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/visibility")
    @Operation(summary = "Update my portfolio's visibility", description = "Sets the master public/private toggle for the authenticated user's portfolio and returns the updated profile.")
    public ResponseEntity<PortfolioProfileDto> updateMyPortfolioVisibility(@Valid @RequestBody UpdateProfileVisibilityRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        // The service now returns the updated profile entity
        PortfolioProfile updatedProfile = portfolioProfileService.updateProfileVisibility(currentUser, request.getIsPublic());
        // Map the updated entity to a DTO to be sent in the response
        PortfolioProfileDto responseDto = portfolioProfileMapper.toDto(updatedProfile);
        // FIX: Ensure the returned DTO is consistent with the updated entity's state.
        responseDto.setPublic(updatedProfile.isPublic());

        return ResponseEntity.ok(responseDto);
    }
}