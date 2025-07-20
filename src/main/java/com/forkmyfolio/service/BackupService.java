package com.forkmyfolio.service;

import com.forkmyfolio.service.model.PortfolioBackupData;

/**
 * Service responsible for creating data backups.
 */
public interface BackupService {

    /**
     * Creates a complete portfolio data backup for the currently authenticated user.
     *
     * @return A {@link PortfolioBackupData} object containing all portfolio domain entities.
     */
    PortfolioBackupData createBackupForCurrentUser();
}