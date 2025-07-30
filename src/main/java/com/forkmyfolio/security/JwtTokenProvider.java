package com.forkmyfolio.security;

import com.forkmyfolio.config.AppProperties;
import com.forkmyfolio.model.User;
import com.forkmyfolio.security.oauth2.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

/**
 * Unified component for creating and validating JWT tokens for both standard login and OAuth2 flows.
 * This is the single source of truth for all JWT operations in the application.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final AppProperties appProperties;
    private final Key key;

    public JwtTokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        String secret = appProperties.getJwt().getSecret();

        // FIX: Add validation to ensure the JWT secret is configured.
        // This prevents the application from crashing with a cryptic error if the secret is missing.
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT secret key is not configured. Please set the 'app.jwt.secret' property in your environment or application properties.");
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Creates a JWT access token from an Authentication object.
     * The token's subject will be the user's database ID.
     *
     * @param authentication The Authentication object from Spring Security.
     * @return A signed JWT string.
     */
    public String createToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        Long userId;

        if (principal instanceof UserPrincipal) {
            userId = ((UserPrincipal) principal).getId();
        } else if (principal instanceof User) {
            userId = ((User) principal).getId();
        } else {
            throw new IllegalArgumentException("Cannot create token for unsupported principal type: " + principal.getClass().getName());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwt().getExpirationMs());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extracts the user ID from the JWT token's subject.
     *
     * @param token The JWT token.
     * @return The user ID (as Long).
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Validates JWT token.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException ex) {
            logger.error("Invalid JWT signature");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}