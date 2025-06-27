package com.forkmyfolio.controller;

import com.forkmyfolio.dto.AuthResponse;
import com.forkmyfolio.dto.LoginRequest;
import com.forkmyfolio.dto.RegisterRequest;
import com.forkmyfolio.dto.UserDto;
import com.forkmyfolio.dto.response.ApiResponseWrapper;
import com.forkmyfolio.exception.TokenRefreshException;
import com.forkmyfolio.model.RefreshToken;
import com.forkmyfolio.model.User;
import com.forkmyfolio.security.JwtTokenProvider;
import com.forkmyfolio.service.RefreshTokenService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/auth") // Ensure consistent base path with other /api/v1 controllers if desired, or keep /auth
@Tag(name = "Authentication", description = "Endpoints for user registration, login, logout, and token refresh. Authentication uses JWT access tokens (short-lived, in response body) and Refresh Tokens (long-lived, in HttpOnly cookies).")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenDurationMs;

    @Value("${app.cookie.secure}") // new property: true in prod, false in dev for http testing
    private boolean cookieSecure;

    @Value("${app.cookie.samesite}") // new property: "Strict" or "Lax"
    private String cookieSameSite;


    /**
     * Constructs an AuthController with necessary dependencies.
     *
     * @param authenticationManager Manages the authentication process.
     * @param userService           Service for user-related operations.
     * @param tokenProvider         Utility for JWT generation and validation.
     * @param refreshTokenService   Service for managing refresh tokens.
     */
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtTokenProvider tokenProvider,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest DTO containing user registration details.
     * @return ResponseEntity containing an {@link AuthResponse} with JWT and user details on success,
     * or an error message if registration fails (e.g., email already exists).
     */
    @Operation(summary = "Register a new user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
            })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(
                    new ApiResponseWrapper<>(null, "Email address already in use!"),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 1. Register and save user
        User registeredUser = userService.registerUser(registerRequest);

        // 2. Manually authenticate without hitting AuthenticationManager
        UserDetails userDetails = userService.loadUserByUsername(registeredUser.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate access token
        //String jwt = tokenProvider.generateToken(authentication); ❌
        //String jwt = tokenProvider.generateToken((UserDetails) userPrincipal); // ✅ safe!
        String jwt = tokenProvider.generateToken(authentication);// ✅ safe!


        // 4. Create and send refresh token cookie
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(registeredUser);
        ResponseCookie refreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, refreshToken.getToken())
                .httpOnly(true)
                .secure(cookieSecure) // Should be true in production
                .path("/api/v1/auth") // Scope cookie to auth paths
                .maxAge(refreshTokenDurationMs / 1000) // convert ms to seconds
                .sameSite(cookieSameSite) // "Strict" or "Lax"
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 5. Return AuthResponse
        UserDto userDto = userService.convertToDto(registeredUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseWrapper<>(new AuthResponse(jwt, userDto)));
    }


    /**
     * Authenticates an existing user, returns a JWT access token in the body,
     * and a refresh token in an HTTP-only cookie.
     *
     * @param loginRequest DTO containing user login credentials.
     * @param response     HttpServletResponse to set the cookie.
     * @return ResponseEntity containing an {@link AuthResponse} with JWT and user details.
     */
    @Operation(summary = "Login an existing user",
            description = "Authenticates user and returns JWT access token in response body, and refresh token in an HttpOnly cookie. The cookie path is /api/v1/auth.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User logged in successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
            })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User userPrincipal = (User) authentication.getPrincipal();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal);
        ResponseCookie refreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, refreshToken.getToken())
                .httpOnly(true)
                .secure(cookieSecure) // Should be true in production
                .path("/api/v1/auth") // Scope cookie to auth paths
                .maxAge(refreshTokenDurationMs / 1000) // convert ms to seconds
                .sameSite(cookieSameSite) // "Strict" or "Lax"
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        UserDto userDto = userService.convertToDto(userPrincipal);

        return ResponseEntity.ok(new ApiResponseWrapper<>(new AuthResponse(jwt, userDto)));
    }

    /**
     * Refreshes an access token using a refresh token provided in an HTTP-only cookie.
     * The refresh token cookie is expected to be sent automatically by the browser.
     * This endpoint implements a rolling refresh token strategy.
     *
     * @param request  HttpServletRequest to retrieve the cookie.
     * @param response HttpServletResponse to set the new refresh token cookie if rotated.
     * @return ResponseEntity containing a new {@link AuthResponse} (with new access token) or an error.
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token",
            description = "Uses the refresh token (sent as an HttpOnly cookie by the browser from the /api/v1/auth path) to issue a new JWT access token. " +
                    "Implements a rolling refresh token strategy: a new refresh token is also issued and set in an HttpOnly cookie.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Access token refreshed successfully. New access token in body, new refresh token in HttpOnly cookie.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Refresh token cookie is missing, invalid, or expired.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
            })
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookieName);
        if (cookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new com.forkmyfolio.dto.response.ApiResponseWrapper(null, "Refresh token cookie not found."));
        }

        String requestRefreshToken = cookie.getValue();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(verifiedRefreshToken -> {
                    User user = verifiedRefreshToken.getUser();
                    // Generate new access token
                    String newAccessToken = tokenProvider.generateToken(user); // generateToken needs to accept User or UserDetails

                    // Implement rolling refresh token: delete old, create new
                    refreshTokenService.deleteByToken(verifiedRefreshToken.getToken());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

                    ResponseCookie newRefreshTokenCookie = ResponseCookie.from(refreshTokenCookieName, newRefreshToken.getToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/api/v1/auth")
                            .maxAge(refreshTokenDurationMs / 1000)
                            .sameSite(cookieSameSite)
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

                    UserDto userDto = userService.convertToDto(user); // Or fetch fresh user details if needed
                    return ResponseEntity.ok(new ApiResponseWrapper<>(new AuthResponse(newAccessToken, userDto)));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database!"));
    }

    /**
     * Logs out the current user by invalidating their refresh token and clearing the cookie.
     *
     * @param request  HttpServletRequest to retrieve refresh token cookie if needed.
     * @param response HttpServletResponse to clear the cookie.
     * @return ResponseEntity indicating logout success, wrapped in ApiResponseWrapper.
     */
    @Operation(summary = "Logout the current user",
            description = "Invalidates the user's refresh token on the server and clears the refresh token cookie.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User logged out successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseWrapper.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if called without valid session/token for some strategies",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseWrapper.class)))
            })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseWrapper<Object>> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookieName);
        if (cookie != null && cookie.getValue() != null) {
            refreshTokenService.deleteByToken(cookie.getValue());
        }

        ResponseCookie emptyCookie = ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth")
                .maxAge(0) // Expire immediately
                .sameSite(cookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, emptyCookie.toString());

        SecurityContextHolder.clearContext();
        // Using Collections.singletonMap for a simple structured message in the 'data' field.
        // Or could return ApiResponseWrapper<String> with data="User logged out successfully."
        // Or ApiResponseWrapper<Object> with data=null and rely on status="success".
        // For consistency, let's use a map for the message.
        return ResponseEntity.ok(new ApiResponseWrapper<>(Collections.singletonMap("message", "User logged out successfully.")));
    }
}
