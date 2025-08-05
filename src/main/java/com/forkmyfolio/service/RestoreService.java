package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.model.User;

/**
 * Service responsible for restoring portfolio data from a backup.
 */
public interface RestoreService {

    /**
     * Restores all portfolio data for the currently authenticated user from a backup DTO.
     * This is a destructive operation and will replace existing portfolio data.
     *
     * @param backupDto The DTO containing the complete portfolio backup.
     */
    void restoreFromBackup(PortfolioBackupDto backupDto);

    void restoreForSpecificUser(PortfolioBackupDto portfolioDto, User restoredUser);
}