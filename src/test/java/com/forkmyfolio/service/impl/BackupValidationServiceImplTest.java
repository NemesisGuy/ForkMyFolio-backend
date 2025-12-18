package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.backup.BackupMetaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackupValidationServiceImplTest {

    private BackupValidationServiceImpl validationService;
    private BackupMetaDto metaDto;

    @BeforeEach
    void setUp() {
        validationService = new BackupValidationServiceImpl();
        metaDto = BackupMetaDto.builder()
                .type("system_backup")
                .compatibility(BackupMetaDto.Compatibility.builder()
                        .minSupportedVersion("2.0.0")
                        .build())
                .build();
    }

    @Test
    void validateBackup_withValidMeta_shouldNotThrowException() {
        assertDoesNotThrow(() -> {
            validationService.validateBackup(metaDto, "system_backup");
        });
    }

    @Test
    void validateBackup_withNullMeta_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.validateBackup(null, "system_backup");
        });
    }

    @Test
    void validateBackup_withNullCompatibility_shouldThrowIllegalArgumentException() {
        metaDto.setCompatibility(null);
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.validateBackup(metaDto, "system_backup");
        });
    }

    @Test
    void validateBackup_withMismatchedType_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.validateBackup(metaDto, "user_backup");
        });
    }

    @Test
    void validateBackup_withIncompatibleVersion_shouldThrowIllegalArgumentException() {
        metaDto.getCompatibility().setMinSupportedVersion("1.0.0");
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.validateBackup(metaDto, "system_backup");
        });
    }
}
