package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.model.User;

import java.util.List;

/**
 * Service for handling backup-related business logic, such as creating backup
 * data structures.
 */
public interface BackupService {

    PortfolioBackupDto createBackupDtoForUser(User user);

    PortfolioBackupDto createBackupDtoForUser(java.util.UUID userUuid);

    /**
     * Creates a full system backup containing all users and their portfolio data.
     * This method is designed to be transactional to support lazy loading of
     * collections.
     *
     * @return A list of UserFullBackupDto objects representing the entire system
     *         state.
     */
    List<UserFullBackupDto> createFullSystemBackup();

}