package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PortfolioDto;
import com.forkmyfolio.exception.ResourceNotFoundException;

/**
 * Service for assembling and retrieving full public portfolios.
 */
public interface PortfolioService {

    /**
     * Retrieves a complete, publicly visible portfolio for a user based on their slug.
     * This method aggregates all visible portfolio components (profile, projects, skills, etc.)
     * into a single DTO.
     *
     * @param slug The unique, URL-friendly identifier of the user.
     * @return A {@link PortfolioDto} containing the user's public portfolio data.
     * @throws ResourceNotFoundException if no active user with the given slug is found.
     */
    PortfolioDto getFullPublicPortfolioBySlug(String slug);
}