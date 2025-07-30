package com.forkmyfolio.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service for handling system-wide backup and restore operations.
 */
public interface BackupRestoreService {

    /**
     * Generates a JSON string representing a full system backup.
     * This includes all users and their complete portfolio data.
     *
     * @return A JSON string of the entire system's data.
     */
    String generateSystemBackupJson();

    /**
     * Restores the system state from a JSON backup file.
     * WARNING: This is a destructive operation. It will wipe existing data
     * and replace it with the data from the backup file.
     *
     * @param inputStream The input stream of the JSON backup file.
     * @throws IOException if there is an error reading the stream.
     */
    void restoreSystemFromJson(InputStream inputStream) throws IOException;
}