package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.AuthResponse;
import com.forkmyfolio.dto.LoginRequest;
import com.forkmyfolio.dto.RegisterRequest;
import com.forkmyfolio.dto.UserDto;
import com.forkmyfolio.exception.TokenRefreshException;
import com.forkmyfolio.model.RefreshToken;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.security.JwtTokenProvider;
import com.forkmyfolio.service.RefreshTokenService;
import com.forkmyfolio.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Added for test-specific security config
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class AuthControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Order(1) // Ensure it's tried before the main SecurityConfig's filter chain if multiple are present
        public SecurityFilterChain testSpecificFilterChain(HttpSecurity http) throws Exception {
            http
                .securityMatcher("/api/v1/auth/**") // Apply this chain only to auth paths
                .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    // Removed @MockBean for JwtTokenProvider to use the real one from the context
    // @MockBean
    // private JwtTokenProvider jwtTokenProvider;
    @Autowired // Autowire the real JwtTokenProvider
    private JwtTokenProvider jwtTokenProvider;


    @MockBean
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Real one for hashing in registration test

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenDurationMs;


    private User user;
    private UserDto userDto;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        // Password set by passwordEncoder in registerUser test
        user.setProfileImageUrl("http://example.com/image.jpg");
        user.setRoles(Set.of(Role.USER));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userDto = new UserDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getProfileImageUrl(), user.getRoles(), user.getCreatedAt());

        registerRequest = new RegisterRequest("test@example.com", "Test", "User", "password123", "http://example.com/image.jpg", Set.of(Role.USER));
        loginRequest = new LoginRequest("test@example.com", "password123");

        authentication = new UsernamePasswordAuthenticationToken(user, "password123", user.getAuthorities());
    }

    @Test
    void registerUser_shouldReturnAuthResponseAndSetCookie() throws Exception {
        given(userService.existsByEmail(registerRequest.getEmail())).willReturn(false);
        // Simulate actual registration process
        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setEmail(registerRequest.getEmail());
        registeredUser.setFirstName(registerRequest.getFirstName());
        registeredUser.setLastName(registerRequest.getLastName());
        registeredUser.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Simulate encoding
        registeredUser.setProfileImageUrl(registerRequest.getProfileImageUrl());
        registeredUser.setRoles(registerRequest.getRoles().isEmpty() ? Set.of(Role.USER) : registerRequest.getRoles());
        registeredUser.setCreatedAt(LocalDateTime.now());
        registeredUser.setUpdatedAt(LocalDateTime.now());

        given(userService.registerUser(any(RegisterRequest.class))).willReturn(registeredUser);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        // Removed: given(jwtTokenProvider.generateToken(authentication)).willReturn("test-access-token");

        RefreshToken refreshToken = new RefreshToken(registeredUser, UUID.randomUUID().toString(), Instant.now().plusMillis(refreshTokenDurationMs));
        given(refreshTokenService.createRefreshToken(registeredUser)).willReturn(refreshToken);
        given(userService.convertToDto(registeredUser)).willReturn(userDto);


        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.accessToken", is(notNullValue()))) // Changed assertion
                .andExpect(jsonPath("$.data.user.email", is(user.getEmail())))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        String setCookieHeader = mvcResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains(refreshTokenCookieName + "=" + refreshToken.getToken()));
        assertTrue(setCookieHeader.contains("HttpOnly"));
        // assertTrue(setCookieHeader.contains("Secure")); // In dev, app.cookie.secure=false
        assertTrue(setCookieHeader.contains("Path=/api/v1/auth"));
        assertTrue(setCookieHeader.contains("SameSite=Lax"));
    }

    @Test
    void authenticateUser_shouldReturnAuthResponseAndSetCookie() throws Exception {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        // Removed: given(jwtTokenProvider.generateToken(authentication)).willReturn("test-access-token");

        RefreshToken refreshToken = new RefreshToken(user, UUID.randomUUID().toString(), Instant.now().plusMillis(refreshTokenDurationMs));
        given(refreshTokenService.createRefreshToken(user)).willReturn(refreshToken);
        given(userService.convertToDto(user)).willReturn(userDto);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.accessToken", is(notNullValue()))) // Changed assertion
                .andExpect(jsonPath("$.data.user.email", is(user.getEmail())))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        String setCookieHeader = mvcResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains(refreshTokenCookieName + "=" + refreshToken.getToken()));
        assertTrue(setCookieHeader.contains("HttpOnly"));
    }

    @Test
    void refreshToken_withValidCookie_shouldReturnNewTokensAndSetCookie() throws Exception {
        String oldRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken validRefreshToken = new RefreshToken(user, oldRefreshTokenValue, Instant.now().plusMillis(refreshTokenDurationMs));

        // String newAccessToken = "new-access-token"; // Real token will be generated
        String newRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = new RefreshToken(user, newRefreshTokenValue, Instant.now().plusMillis(refreshTokenDurationMs));

        given(refreshTokenService.findByToken(oldRefreshTokenValue)).willReturn(Optional.of(validRefreshToken));
        given(refreshTokenService.verifyExpiration(validRefreshToken)).willReturn(validRefreshToken);
        // Removed: given(jwtTokenProvider.generateToken(user)).willReturn(newAccessToken);
        doNothing().when(refreshTokenService).deleteByToken(oldRefreshTokenValue);
        given(refreshTokenService.createRefreshToken(user)).willReturn(newRefreshToken);
        given(userService.convertToDto(user)).willReturn(userDto);

        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, oldRefreshTokenValue);
        refreshTokenCookie.setPath("/api/v1/auth"); // Ensure path matches what controller expects/sets

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.accessToken", is(notNullValue()))) // Changed assertion
                .andExpect(jsonPath("$.data.user.email", is(user.getEmail())))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        String setCookieHeader = mvcResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains(refreshTokenCookieName + "=" + newRefreshTokenValue));
        assertTrue(setCookieHeader.contains("HttpOnly"));
    }

    @Test
    void refreshToken_withMissingCookie_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("unauthorized")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("refreshToken")))
                .andExpect(jsonPath("$.errors[0].message", is("Refresh token cookie not found.")));
    }

    @Test
    void refreshToken_withInvalidTokenInCookie_shouldReturnUnauthorized() throws Exception {
        String invalidTokenValue = "invalid-token";
        // Simulate service throwing the exception that GlobalExceptionHandler will catch
        given(refreshTokenService.findByToken(invalidTokenValue))
            .willThrow(new TokenRefreshException(invalidTokenValue, "Refresh token not found in database!"));

        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, invalidTokenValue);
        refreshTokenCookie.setPath("/api/v1/auth");

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized()) // TokenRefreshException is now mapped to 401 by GlobalExceptionHandler
                .andExpect(jsonPath("$.status", is("unauthorized")))
                .andExpect(jsonPath("$.errors[0].field", is("refreshToken")))
                .andExpect(jsonPath("$.errors[0].message", is("Failed for [invalid-token]: Refresh token not found in database!")));
    }

    @Test
    void refreshToken_withExpiredTokenInCookie_shouldReturnUnauthorized() throws Exception {
        String expiredTokenValue = UUID.randomUUID().toString();
        RefreshToken expiredRefreshToken = new RefreshToken(user, expiredTokenValue, Instant.now().minusMillis(10000)); // Expired

        given(refreshTokenService.findByToken(expiredTokenValue)).willReturn(Optional.of(expiredRefreshToken));
        given(refreshTokenService.verifyExpiration(expiredRefreshToken))
            .willThrow(new TokenRefreshException(expiredTokenValue, "Refresh token was expired. Please make a new sign-in request"));

        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, expiredTokenValue);
        refreshTokenCookie.setPath("/api/v1/auth");

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized()) // TokenRefreshException mapped to 401
                .andExpect(jsonPath("$.status", is("unauthorized")))
                .andExpect(jsonPath("$.errors[0].field", is("refreshToken")))
                .andExpect(jsonPath("$.errors[0].message", is(String.format("Failed for [%s]: Refresh token was expired. Please make a new sign-in request", expiredTokenValue))));
    }

    @Test
    void logoutUser_shouldClearCookieAndReturnSuccess() throws Exception {
        String tokenValue = UUID.randomUUID().toString();
        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, tokenValue);
        refreshTokenCookie.setPath("/api/v1/auth");

        doNothing().when(refreshTokenService).deleteByToken(tokenValue);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.message", is("User logged out successfully.")))
                .andReturn();

        String setCookieHeader = mvcResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains(refreshTokenCookieName + "=")); // Cleared value
        assertTrue(setCookieHeader.contains("Max-Age=0"));
        assertTrue(setCookieHeader.contains("HttpOnly"));
    }
}
