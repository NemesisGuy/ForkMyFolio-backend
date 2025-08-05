package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.model.User;

/**
 * Service responsible for creating data backups.
 */
public interface BackupService {

    /**
     * Creates a complete portfolio data transfer object for the currently authenticated user.
     *
     * @return A {@link PortfolioBackupDto} object containing all portfolio data ready for response.
     */
    PortfolioBackupDto createBackupForCurrentUser();

    PortfolioBackupDto createBackupForUser(User user);
}