package com.forkmyfolio.controller;

import com.forkmyfolio.dto.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.security.JwtTokenProvider;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user authentication requests, including registration and login.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    /**
     * Constructs an AuthController with necessary dependencies.
     * @param authenticationManager Manages the authentication process.
     * @param userService Service for user-related operations.
     * @param tokenProvider Utility for JWT generation and validation.
     */
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest DTO containing user registration details.
     * @return ResponseEntity containing an {@link AuthResponse} with JWT and user details on success,
     *         or an error message if registration fails (e.g., email already exists).
     */
    @Operation(summary = "Register a new user",
               responses = {
                   @ApiResponse(responseCode = "201", description = "User registered successfully",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.ApiResponse.class)))
               })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(new com.forkmyfolio.dto.ApiResponse(false, "Email address already in use!"),
                                      HttpStatus.BAD_REQUEST);
        }

        User registeredUser = userService.registerUser(registerRequest);

        // Authenticate the user immediately after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserDto userDto = userService.convertToDto(registeredUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwt, userDto));
    }

    /**
     * Authenticates an existing user and returns a JWT.
     *
     * @param loginRequest DTO containing user login credentials.
     * @return ResponseEntity containing an {@link AuthResponse} with JWT and user details on successful authentication.
     */
    @Operation(summary = "Login an existing user",
               responses = {
                   @ApiResponse(responseCode = "200", description = "User logged in successfully",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                   @ApiResponse(responseCode = "401", description = "Invalid credentials",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.ApiResponse.class)))
               })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User userPrincipal = (User) authentication.getPrincipal();
        UserDto userDto = userService.convertToDto(userPrincipal);

        return ResponseEntity.ok(new AuthResponse(jwt, userDto));
    }

    /**
     * Logs out the current user.
     * For JWT, this is typically handled client-side by deleting the token.
     * This endpoint can be used for server-side cleanup if necessary (e.g., token blacklisting).
     *
     * @return ResponseEntity indicating logout success.
     */
    @Operation(summary = "Logout the current user",
               description = "For JWT-based authentication, logout is primarily a client-side operation (deleting the token). This endpoint is a placeholder or can be used for server-side token invalidation strategies if implemented.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "User logged out successfully (client should clear token)",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.ApiResponse.class)))
               })
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // In a stateless JWT setup, actual logout is clearing the token on the client.
        // If a token blacklist is implemented, this is where you'd add the token to it.
        // For now, just acknowledge the request.
        SecurityContextHolder.clearContext(); // Clear server-side security context if any
        return ResponseEntity.ok(new com.forkmyfolio.dto.ApiResponse(true, "User logged out successfully. Please clear your token."));
    }
}
