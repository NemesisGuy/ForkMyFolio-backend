package com.forkmyfolio.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.BackupRestoreService;
import com.forkmyfolio.service.model.SystemBackup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupRestoreServiceImpl implements BackupRestoreService {

    // Inject all repositories needed to fetch and wipe data
    private final UserRepository userRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final TestimonialRepository testimonialRepository;
    private final QualificationRepository qualificationRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final SettingRepository settingRepository;
    private final UserSettingRepository userSettingRepository;

    // ObjectMapper configured to handle Java 8 time types
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    @Transactional(readOnly = true)
    public String generateSystemBackupJson() {
        log.info("Starting system backup generation.");
        try {
            SystemBackup backup = new SystemBackup();
            // Fetch all data from repositories
            backup.setUsers(userRepository.findAll());
            backup.setPortfolioProfiles(portfolioProfileRepository.findAll());
            backup.setProjects(projectRepository.findAll());
            backup.setSkills(skillRepository.findAll());
            backup.setExperiences(experienceRepository.findAll());
            backup.setTestimonials(testimonialRepository.findAll());
            backup.setQualifications(qualificationRepository.findAll());
            backup.setContactMessages(contactMessageRepository.findAll());
            backup.setSettings(settingRepository.findAll());
            backup.setUserSettings(userSettingRepository.findAll());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup);
            log.info("System backup generation completed successfully.");
            return json;
        } catch (Exception e) {
            log.error("Error during system backup generation", e);
            // Wrap in a runtime exception to ensure transaction rollback on failure
            throw new RuntimeException("Failed to generate system backup", e);
        }
    }

    @Override
    @Transactional
    public void restoreSystemFromJson(InputStream inputStream) throws IOException {
        log.warn("Starting system restore. THIS IS A DESTRUCTIVE OPERATION.");

        // 1. Read and parse the backup file into our DTO
        SystemBackup backup = objectMapper.readValue(inputStream, new TypeReference<SystemBackup>() {});
        log.info("Backup file parsed successfully. Contains {} users.", backup.getUsers().size());

        // 2. Wipe existing data in the correct order to respect foreign key constraints
        log.info("Wiping existing data...");
        // Delete entities that have dependencies on them last
        contactMessageRepository.deleteAllInBatch();
        qualificationRepository.deleteAllInBatch();
        testimonialRepository.deleteAllInBatch();
        experienceRepository.deleteAllInBatch();
        skillRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        portfolioProfileRepository.deleteAllInBatch();
        userSettingRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch(); // Users are deleted before settings
        settingRepository.deleteAllInBatch();
        log.info("Existing data wiped successfully.");

        // 3. Restore data from the backup object
        // The saveAll methods will insert the entities. The database will generate new primary keys.
        // This approach works for a full-state restore where inter-entity relationships are preserved
        // within the backup object itself (e.g., a Project object contains its User object).
        log.info("Restoring data from backup...");
        settingRepository.saveAll(backup.getSettings());
        userRepository.saveAll(backup.getUsers());
        portfolioProfileRepository.saveAll(backup.getPortfolioProfiles());
        projectRepository.saveAll(backup.getProjects());
        skillRepository.saveAll(backup.getSkills());
        experienceRepository.saveAll(backup.getExperiences());
        testimonialRepository.saveAll(backup.getTestimonials());
        qualificationRepository.saveAll(backup.getQualifications());
        contactMessageRepository.saveAll(backup.getContactMessages());
        userSettingRepository.saveAll(backup.getUserSettings());

        log.warn("System restore completed successfully.");
    }
}