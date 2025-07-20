package com.forkmyfolio.security;

import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    private final long jwtExpirationInMs = 3600000; // 1 hour
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;
    private User user;
    private Authentication authentication;
    private String jwtTestSecret; // Will be generated

    @BeforeEach
    void setUp() {
        // Generate a secure key for HS512 and get its Base64 encoded string
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        this.jwtTestSecret = java.util.Base64.getEncoder().encodeToString(key.getEncoded());

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", this.jwtTestSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", jwtExpirationInMs);
        jwtTokenProvider.init(); // Manually call init to set up the key based on the test secret

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(Set.of(Role.USER, Role.ADMIN));

        authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void generateToken_withAuthentication_shouldCreateValidToken() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length); // Basic check for JWT structure

        Long userIdFromToken = jwtTokenProvider.getUserIdFromJWT(token);
        assertEquals(user.getId(), userIdFromToken);

        Claims claims = jwtTokenProvider.getClaimsFromJWT(token);
        assertEquals(user.getEmail(), claims.get("email", String.class));
        assertTrue(claims.get("roles", String.class).contains("ROLE_USER"));
        assertTrue(claims.get("roles", String.class).contains("ROLE_ADMIN"));
    }

    @Test
    void generateToken_withUserObject_shouldCreateValidToken() {
        String token = jwtTokenProvider.generateToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);

        Long userIdFromToken = jwtTokenProvider.getUserIdFromJWT(token);
        assertEquals(user.getId(), userIdFromToken);

        Claims claims = jwtTokenProvider.getClaimsFromJWT(token);
        assertEquals(user.getEmail(), claims.get("email", String.class));
        assertTrue(claims.get("roles", String.class).contains("ROLE_USER"));
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // Generate a token with negative expiration to make it expired instantly
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", -1000L); // -1 second
        jwtTokenProvider.init(); // Re-init with new expiration
        String expiredToken = jwtTokenProvider.generateToken(authentication);

        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", jwtExpirationInMs);
        jwtTokenProvider.init();

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void validateToken_withInvalidSignature_shouldReturnFalse() {
        String token = jwtTokenProvider.generateToken(authentication);
        // Tamper with the token signature part
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tamperedSignaturePart";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    void validateToken_withMalformedToken_shouldReturnFalse() {
        String malformedToken = "this.is.not.a.jwt";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    void validateToken_withUnsupportedToken_shouldReturnFalse() {
        // Create a token with a different algorithm or structure that's not supported
        // This is harder to simulate perfectly without more complex JWT lib usage,
        // but an empty or fundamentally different string might trigger it.
        // Jwts.builder().subject("test").compact(); // Example of token without signature
        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0."; // Unsigned token
        assertFalse(jwtTokenProvider.validateToken(unsupportedToken));
    }

    @Test
    void validateToken_withEmptyClaims_shouldReturnFalse() {
        // This would typically be caught by other validation (malformed, signature)
        // if a token is truly empty or claims string is empty.
        // IllegalArgumentException for empty claims string is handled.
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken("  "));
    }

    @Test
    void getUserIdFromJWT_shouldExtractCorrectId() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertEquals(user.getId(), jwtTokenProvider.getUserIdFromJWT(token));
    }

    @Test
    void getClaimsFromJWT_shouldExtractCorrectClaims() {
        String token = jwtTokenProvider.generateToken(user);
        Claims claims = jwtTokenProvider.getClaimsFromJWT(token);
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getEmail(), claims.get("email", String.class));
        String rolesClaim = claims.get("roles", String.class);
        assertTrue(rolesClaim.contains(Role.USER.name())); // Assuming roles are stored as "ROLE_USER", "ROLE_ADMIN"
    }

}
