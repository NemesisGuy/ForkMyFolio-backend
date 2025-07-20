package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PortfolioBackupDto;

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
}