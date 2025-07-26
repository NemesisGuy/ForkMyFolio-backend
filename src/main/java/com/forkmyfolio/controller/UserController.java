package com.forkmyfolio.controller;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user-related operations, such as fetching user profiles.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints for user operations")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Constructs a UserController with the necessary UserService.
     *
     * @param userService The service for user-related business logic.
     */
    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * The user's password and other sensitive details are excluded.
     *
     * @return ResponseEntity containing the {@link UserDto} of the current user.
     */
    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()") // Ensures the user is authenticated
    @Operation(summary = "Get current user profile",
            description = "Fetches the profile information for the currently authenticated user. Excludes sensitive data like password.",
            security = @SecurityRequirement(name = "bearerAuth"), // References the security scheme in OpenApiConfig
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponseWrapper.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponseWrapper.class))),
                    @ApiResponse(responseCode = "404", description = "User not found (should not happen if authenticated)",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponseWrapper.class)))
            })
    public UserDto getCurrentUserProfile() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return userMapper.toDto(currentUser);
    }
}
