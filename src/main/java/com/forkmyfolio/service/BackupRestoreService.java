package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.UserFullBackupDto;

import java.util.List;

/**
 * Service for handling system-wide backup and restore operations.
 */
public interface BackupRestoreService {

    /**
     * Creates a list of DTOs representing a full system backup.
     * This includes all users and their complete portfolio data.
     *
     * @return A list of {@link UserFullBackupDto} containing the system's data.
     */
    List<UserFullBackupDto> createSystemBackupData();

    /**
     * Restores the system state from a list of backup DTOs.
     * WARNING: This is a destructive operation. It will wipe existing data
     * and replace it with the data from the backup.
     *
     * @param data The list of DTOs containing the backup data.
     */
    void restoreSystemFromData(List<UserFullBackupDto> data);

}