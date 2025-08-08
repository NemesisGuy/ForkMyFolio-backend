package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.model.User;

/**
 * Service for handling backup-related business logic, such as creating backup data structures.
 */
public interface BackupService {

    PortfolioBackupDto createBackupDtoForUser(User user);

}