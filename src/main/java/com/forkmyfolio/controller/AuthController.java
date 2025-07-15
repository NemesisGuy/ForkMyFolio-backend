package com.forkmyfolio.controller;

import com.forkmyfolio.dto.response.AuthResponse;
import com.forkmyfolio.dto.request.LoginRequest;
import com.forkmyfolio.dto.request.RegisterRequest;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.ApiResponseWrapper;
import com.forkmyfolio.exception.TokenRefreshException;
import com.forkmyfolio.mapper.UserMapper; // <-- IMPORT MAPPER
import com.forkmyfolio.model.RefreshToken;
import com.forkmyfolio.model.User;
import com.forkmyfolio.security.JwtTokenProvider;
import com.forkmyfolio.service.RefreshTokenService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import java.util.Collections;

/**
 * Controller for handling user authentication requests, including registration and login.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, logout, and token refresh.")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper; // <-- 1. INJECT MAPPER

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshTokenCookieName;
    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenDurationMs;
    @Value("${app.cookie.secure}")
    private boolean cookieSecure;
    @Value("${app.cookie.samesite}")
    private String cookieSameSite;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtTokenProvider tokenProvider, RefreshTokenService refreshTokenService, UserMapper userMapper) { // <-- 2. ADD TO CONSTRUCTOR
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper; // <-- 2. ADD TO CONSTRUCTOR
    }

    /**
     * Registers a new user in the system.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        logger.info("Received registration request for email: {}", registerRequest.getEmail());
        logger.debug("Registration request details: {}", registerRequest);

        if (userService.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email '{}' already in use.", registerRequest.getEmail());
            return new ResponseEntity<>(new ApiResponseWrapper<>(null, "Email address already in use!"), HttpStatus.BAD_REQUEST);
        }

        // 3. FIX: Call the DTO-less service method with primitive values from the DTO
        User registeredUser = userService.registerUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getProfileImageUrl(),
                registerRequest.getRoles()
        );
        logger.info("User with email '{}' registered successfully with ID: {}", registeredUser.getEmail(), registeredUser.getId());

        // --- The rest of the logic remains the same until the final conversion ---
        UserDetails userDetails = (UserDetails) registeredUser; // The User model implements UserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("User '{}' authenticated programmatically after registration.", registeredUser.getEmail());

        String jwt = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(registeredUser);
        logger.info("Generated new JWT and refresh token for user '{}'.", registeredUser.getEmail());

        ResponseCookie refreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, refreshToken.getToken())
                .httpOnly(true).secure(cookieSecure).path("/api/v1/auth")
                .maxAge(refreshTokenDurationMs / 1000).sameSite(cookieSameSite).build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        logger.info("Set refresh token cookie for user '{}'.", registeredUser.getEmail());

        // 4. FIX: Use the mapper to convert the User entity to a UserDto for the response
        UserDto userDto = userMapper.toDto(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseWrapper<>(new AuthResponse(jwt, userDto)));
    }

    /**
     * Authenticates an existing user.
     */
    @PostMapping("/login")
    @Operation(summary = "Login an existing user")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        logger.info("Received login request for email: {}", loginRequest.getEmail());
        logger.debug("Login request details: {}", loginRequest);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        logger.info("Authentication successful for user '{}'.", loginRequest.getEmail());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User userPrincipal = (User) authentication.getPrincipal();
        logger.info("Generated new JWT for user '{}'.", userPrincipal.getEmail());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal);
        ResponseCookie refreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, refreshToken.getToken())
                .httpOnly(true).secure(cookieSecure).path("/api/v1/auth")
                .maxAge(refreshTokenDurationMs / 1000).sameSite(cookieSameSite).build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        logger.info("Created new refresh token and set cookie for user '{}'.", userPrincipal.getEmail());

        // 5. FIX: Use the mapper to convert the User principal to a UserDto
        UserDto userDto = userMapper.toDto(userPrincipal);
        return ResponseEntity.ok(new ApiResponseWrapper<>(new AuthResponse(jwt, userDto)));
    }

    /**
     * Refreshes an access token.
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Received request to refresh token.");
        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookieName);

        if (cookie == null) {
            logger.warn("Token refresh request failed: No refresh token cookie found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseWrapper<>(null, "Refresh token cookie not found."));
        }

        String requestRefreshToken = cookie.getValue();
        logger.debug("Found refresh token cookie. Token value (first 8 chars): {}", requestRefreshToken.substring(0, Math.min(requestRefreshToken.length(), 8)));

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(verifiedRefreshToken -> {
                    User user = verifiedRefreshToken.getUser();
                    logger.info("Refresh token verified for user '{}'. Generating new tokens.", user.getEmail());

                    String newAccessToken = tokenProvider.generateToken(user);

                    refreshTokenService.deleteByToken(verifiedRefreshToken.getToken());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
                    logger.info("Rolled refresh token for user '{}'.", user.getEmail());

                    ResponseCookie newRefreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, newRefreshToken.getToken())
                            .httpOnly(true).secure(cookieSecure).path("/api/v1/auth")
                            .maxAge(refreshTokenDurationMs / 1000).sameSite(cookieSameSite).build();
                    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
                    logger.info("Set new refresh token cookie for user '{}'.", user.getEmail());

                    // 6. FIX: Use the mapper to convert the User to a UserDto
                    UserDto userDto = userMapper.toDto(user);
                    return ResponseEntity.ok(new ApiResponseWrapper<>(new AuthResponse(newAccessToken, userDto)));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database!"));
    }

    /**
     * Logs out the current user.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout the current user")
    public ResponseEntity<ApiResponseWrapper<Object>> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // ... This method does not deal with DTOs, so no changes are needed here. It remains correct.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (authentication != null && !"anonymousUser".equals(authentication.getPrincipal())) ? authentication.getName() : "anonymous";
        logger.info("Received logout request from user: {}", userEmail);

        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookieName);
        if (cookie != null && cookie.getValue() != null) {
            refreshTokenService.deleteByToken(cookie.getValue());
            logger.info("Deleted refresh token from database for user '{}'.", userEmail);
        } else {
            logger.warn("Logout request for user '{}' did not have a refresh token cookie to invalidate.", userEmail);
        }

        ResponseCookie emptyCookie = ResponseCookie.from(refreshTokenCookieName, "").httpOnly(true)
                .secure(cookieSecure).path("/api/v1/auth").maxAge(0).sameSite(cookieSameSite).build();
        response.addHeader(HttpHeaders.SET_COOKIE, emptyCookie.toString());
        logger.info("Cleared refresh token cookie for user '{}'.", userEmail);

        SecurityContextHolder.clearContext();
        logger.info("Cleared SecurityContext for user '{}'. Logout complete.", userEmail);

        return ResponseEntity.ok(new ApiResponseWrapper<>(Collections.singletonMap("message", "User logged out successfully.")));
    }
}