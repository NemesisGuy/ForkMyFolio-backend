package com.forkmyfolio.controller;

import com.forkmyfolio.dto.UserDto;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.UserService;
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
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Public endpoint for viewing the portfolio owner's profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final UserService userService;
    private final UserMapper userMapper; // <-- INJECT MAPPER

    @Autowired
    public ProfileController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper; // <-- INJECT MAPPER
    }

    /**
     * Retrieves the public profile of the portfolio owner.
     *
     * @return The UserDto of the portfolio owner.
     */
    @GetMapping
    @Operation(summary = "Get the public portfolio profile", description = "Retrieves the main profile information for the portfolio owner.")
    public UserDto getPublicProfile() {
        logger.info("Received request for public portfolio profile.");
        // 1. Call service to get the domain model
        User profileEntity = userService.getPublicProfile();
        logger.info("Successfully retrieved public profile entity for user: {}", profileEntity.getEmail());

        // 2. Use mapper to convert domain model to DTO for the response
        return userMapper.toDto(profileEntity);
    }
}