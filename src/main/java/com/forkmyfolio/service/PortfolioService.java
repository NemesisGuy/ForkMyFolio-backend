package com.forkmyfolio.service;

import com.forkmyfolio.exception.PermissionDeniedException;
import com.forkmyfolio.model.User;
import com.forkmyfolio.exception.ResourceNotFoundException;

/**
 * Service for retrieving user data for public portfolios.
 */
public interface PortfolioService {

    /**
     * Retrieves a user entity for the purpose of building a public portfolio.
     * This method validates that the user exists and their portfolio is set to public.
     *
     * @param slug The unique, URL-friendly identifier of the user.
     * @return The {@link User} entity if the portfolio is public.
     * @throws ResourceNotFoundException if no active user with the given slug is found.
     * @throws PermissionDeniedException if the user's portfolio is private.
     */
    User getPublicPortfolioUserBySlug(String slug);
}