package com.forkmyfolio.controller;

import com.forkmyfolio.config.AppProperties;
import com.forkmyfolio.dto.request.LoginRequest;
import com.forkmyfolio.dto.request.RegisterRequest;
import com.forkmyfolio.dto.response.AuthResponse;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.exception.TokenRefreshException;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.RefreshToken;
import com.forkmyfolio.model.User;
import com.forkmyfolio.security.JwtTokenProvider;
import com.forkmyfolio.service.AuthService;
import com.forkmyfolio.service.RefreshTokenService;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.impl.VisitorStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, logout, and token refresh.")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final VisitorStatsService visitorStatsService;
    private final AppProperties appProperties;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService, AuthService authService, JwtTokenProvider tokenProvider, RefreshTokenService refreshTokenService, UserMapper userMapper, VisitorStatsService visitorStatsService, AppProperties appProperties) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
        this.visitorStatsService = visitorStatsService;
        this.appProperties = appProperties;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account after they accept the terms of service.")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        logger.info("Received registration request for email: {}", registerRequest.getEmail());
        // The AuthService now handles all registration logic, including setting POPIA compliance fields.
        User registeredUser = authService.registerUser(registerRequest);

        Authentication authentication = new UsernamePasswordAuthenticationToken(registeredUser, null, registeredUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken((User) authentication.getPrincipal());

        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken.getToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        logger.info("Set refresh token cookie for user '{}'.", registeredUser.getEmail());

        UserDto userDto = userMapper.toDto(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwt, userDto));
    }

    @PostMapping("/login")
    @Operation(summary = "Login an existing user")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        logger.info("Received login request for email: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken((User) authentication.getPrincipal());

        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken.getToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        logger.info("Created new refresh token and set cookie for user '{}'.", ((User) authentication.getPrincipal()).getEmail());

        UserDto userDto = userMapper.toDto((User) authentication.getPrincipal());
        return ResponseEntity.ok(new AuthResponse(jwt, userDto));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Received request to refresh token.");
        Cookie cookie = WebUtils.getCookie(request, appProperties.getJwt().getRefreshCookieName());
        if (cookie == null) {
            throw new TokenRefreshException(null, "Refresh token cookie not found. Please log in again.");
        }
        String requestRefreshToken = cookie.getValue();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(verifiedRefreshToken -> {
                    User user = verifiedRefreshToken.getUser();

                    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    String newAccessToken = tokenProvider.createToken(authentication);

                    RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(verifiedRefreshToken);

                    ResponseCookie newRefreshTokenCookie = createRefreshTokenCookie(newRefreshToken.getToken());
                    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
                    logger.info("Rotated refresh token and set new cookie for user '{}'.", user.getEmail());

                    UserDto userDto = userMapper.toDto(user);
                    return ResponseEntity.ok(new AuthResponse(newAccessToken, userDto));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database!"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout the current user")
    public ResponseEntity<Map<String, String>> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (authentication != null && !"anonymousUser".equals(authentication.getPrincipal())) ? authentication.getName() : "anonymous";
        logger.info("Received logout request from user: {}", userEmail);

        Cookie cookie = WebUtils.getCookie(request, appProperties.getJwt().getRefreshCookieName());
        if (cookie != null && cookie.getValue() != null) {
            refreshTokenService.deleteByToken(cookie.getValue());
        }

        ResponseCookie emptyCookie = createRefreshTokenCookie("");
        response.addHeader(HttpHeaders.SET_COOKIE, emptyCookie.toString());
        logger.info("Cleared refresh token cookie for user '{}'.", userEmail);

        SecurityContextHolder.clearContext();
        visitorStatsService.incrementLogoutSuccess();
        return ResponseEntity.ok(Map.of("message", "User logged out successfully."));
    }

    private ResponseCookie createRefreshTokenCookie(String token) {
        long maxAge = StringUtils.hasText(token) ? appProperties.getJwt().getRefreshExpirationMs() / 1000 : 0;

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(appProperties.getJwt().getRefreshCookieName(), token)
                .httpOnly(true)
                .secure(appProperties.getCookie().isSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite(appProperties.getCookie().getSamesite());

        String cookieDomain = appProperties.getSecurity().getCookieDomain();
        if (StringUtils.hasText(cookieDomain)) {
            cookieBuilder.domain(cookieDomain);
            logger.debug("Setting cookie with domain: {}", cookieDomain);
        } else {
            logger.debug("Setting cookie without a specific domain.");
        }

        return cookieBuilder.build();
    }
}