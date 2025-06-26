package com.forkmyfolio.service;

import com.forkmyfolio.model.RefreshToken;
import com.forkmyfolio.model.User;
import com.forkmyfolio.exception.TokenRefreshException;

import java.util.Optional;

/**
 * Service interface for managing refresh tokens.
 * Handles creation, validation, and deletion of refresh tokens.
 */
public interface RefreshTokenService {

    /**
     * Finds a refresh token by its token string.
     *
     * @param token The token string.
     * @return An Optional containing the {@link RefreshToken} if found.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Creates and persists a new refresh token for the given user.
     * If a user already has a refresh token, it might be replaced or a new one added,
     * depending on the application's policy (current design implies replacing existing for simplicity).
     *
     * @param user The user for whom the refresh token is to be created.
     * @return The created {@link RefreshToken}.
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Verifies that the given refresh token is not expired and not revoked (if applicable).
     * Throws an exception if the token is invalid.
     *
     * @param token The refresh token to verify.
     * @return The verified {@link RefreshToken}.
     * @throws TokenRefreshException if the token is expired or invalid.
     */
    RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException;

    /**
     * Deletes all refresh tokens associated with a specific user ID.
     * Typically used during logout or when a user's credentials change.
     *
     * @param userId The ID of the user whose refresh tokens are to be deleted.
     * @return The number of tokens deleted.
     */
    int deleteByUserId(Long userId);

    /**
     * Deletes a specific refresh token by its string value.
     * @param token The refresh token string.
     */
    void deleteByToken(String token);
}
