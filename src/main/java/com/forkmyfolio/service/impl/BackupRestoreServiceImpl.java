package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.UserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.BackupRestoreService;
import com.forkmyfolio.service.BackupService;
import com.forkmyfolio.service.RestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupRestoreServiceImpl implements BackupRestoreService {

    // Repositories for wiping data and fetching users
    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final QualificationRepository qualificationRepository;
    private final TestimonialRepository testimonialRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserSettingRepository userSettingRepository;
    private final SettingRepository settingRepository;
    private final SkillRepository skillRepository;

    // Services and Mappers for creating and restoring data
    private final BackupService backupService;
    private final RestoreService restoreService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserFullBackupDto> createSystemBackupData() {
        log.info("Starting system-wide backup data generation.");
        List<User> allUsers = userRepository.findAll();
        List<UserFullBackupDto> systemBackupData = new ArrayList<>();

        for (User user : allUsers) {
            // For each user, generate their complete portfolio backup
            PortfolioBackupDto portfolioBackup = backupService.createBackupForUser(user);
            // Convert the user entity to a DTO
            UserDto userDto = userMapper.toDto(user);

            // Combine them into the full backup DTO
            systemBackupData.add(new UserFullBackupDto(userDto, portfolioBackup));
        }
        log.info("Successfully generated backup data for {} users.", systemBackupData.size());
        return systemBackupData;
    }

    @Override
    @Transactional
    public void restoreSystemFromData(List<UserFullBackupDto> backupData) {
        log.warn("Starting system restore from backup data. THIS IS A DESTRUCTIVE OPERATION.");

        // 1. Wipe all existing data in the correct order to respect foreign key constraints
        wipeAllData();

        // 2. Restore all users and their portfolios from the backup data
        for (UserFullBackupDto userBackup : backupData) {
            UserDto userDto = userBackup.getUser();
            PortfolioBackupDto portfolioDto = userBackup.getPortfolio();

            // Create the user entity from the DTO
            User user = new User();
            user.setUuid(userDto.getId());
            user.setEmail(userDto.getEmail());
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setSlug(userDto.getSlug());
            user.setProfileImageUrl(userDto.getProfileImageUrl());
            user.setRoles(userDto.getRoles());
            user.setActive(userDto.isActive());
            // Passwords are not included in backups for security. A temporary or random password should be set.
            user.setPassword(passwordEncoder.encode("restored-password-" + UUID.randomUUID()));
            User restoredUser = userRepository.save(user);

            // Now, use the existing RestoreService to restore this new user's portfolio
            restoreService.restoreForSpecificUser(portfolioDto, restoredUser);
        }
        log.warn("System restore completed successfully. {} users restored.", backupData.size());
    }

    private void wipeAllData() {
        log.info("Wiping all existing portfolio and user data...");
        // Order is critical to avoid foreign key constraint violations.
        // Delete entities that depend on others first.
        contactMessageRepository.deleteAllInBatch();
        userSkillRepository.deleteAllInBatch();
        qualificationRepository.deleteAllInBatch();
        testimonialRepository.deleteAllInBatch();
        experienceRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        portfolioProfileRepository.deleteAllInBatch();
        userSettingRepository.deleteAllInBatch();
        // After all dependencies are gone, delete the users and global skills/settings.
        userRepository.deleteAllInBatch();
        skillRepository.deleteAllInBatch();
        settingRepository.deleteAllInBatch();
        log.info("All data wiped successfully.");
    }
}