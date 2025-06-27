package com.forkmyfolio.security;

import com.forkmyfolio.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            logger.error("JWT secret is null or empty. Please check property 'jwt.secret'.");
            throw new IllegalArgumentException("JWT secret cannot be null or empty.");
        }
        logger.info("Initializing JwtTokenProvider with secret: '{}...'", jwtSecret.substring(0, Math.min(jwtSecret.length(), 10)));
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token from Authentication (safe now).
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    /**
     * Preferred method: generates a JWT token from UserDetails.
     * Uses the user's unique identifier (email or username) as subject.
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String email = userDetails.getUsername();
        Long userId = (userDetails instanceof User) ? ((User) userDetails).getId() : null;

        return Jwts.builder()
                .subject(userDetails.getUsername()) // username is typically email
                .claim("userId", userId)
                .claim("roles", authorities)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }




    /*  *//**
     * Extracts username (subject) from JWT token.
     * Note: Since you used username/email as subject, this returns the username string.
     *//*
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }*/

    /**
     * Extracts the username/email (subject) from the JWT token.
     * The subject was set to the user's email during token creation.
     *
     * @param token The JWT token.
     * @return The email/username from the token subject.
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = getClaimsFromJWT(token);
        return claims.getSubject(); // which is the email
    }

    /**
     * Extracts the user ID from the JWT token claims.
     *
     * @param token The JWT token.
     * @return The user ID (as Long).
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = getClaimsFromJWT(token);
        return claims.get("userId", Long.class);
    }


    /**
     * Retrieves claims from the JWT token.
     */
    public Claims getClaimsFromJWT(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates JWT token.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
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
