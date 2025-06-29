package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.UpdateUserAccountRequest;
import com.forkmyfolio.dto.UserDto;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing the admin's own user account details.
 */
@RestController
@RequestMapping("/api/v1/admin/account")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Account Management", description = "Endpoints for managing the admin's own account.")
@SecurityRequirement(name = "bearerAuth")
public class AdminAccountController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountController.class);

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public AdminAccountController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    @Operation(summary = "Get current admin user account", description = "Fetches account info for the logged-in admin.")
    public UserDto getAdminAccount() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested their account details.", currentUser.getEmail());
        return userMapper.toDto(currentUser);
    }

    @PutMapping
    @Operation(summary = "Update admin user account", description = "Updates the admin's own account details (e.g., first and last name).")
    public UserDto updateAdminAccount(@Valid @RequestBody UpdateUserAccountRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is updating their account details.", currentUser.getEmail());
        User updatedUser = userService.updateUserAccount(
                currentUser.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getProfileImageUrl()
        );

        logger.info("Successfully updated account details for user '{}'.", currentUser.getEmail());
        return userMapper.toDto(updatedUser);
    }
}