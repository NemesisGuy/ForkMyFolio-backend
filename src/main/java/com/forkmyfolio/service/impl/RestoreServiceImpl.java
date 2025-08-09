package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.TreeMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreServiceImpl implements RestoreService {

    // Services
    private final UserService userService;
    private final EntityManager entityManager;

    // Repositories for data manipulation
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final QualificationRepository qualificationRepository;
    private final TestimonialRepository testimonialRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final SkillRepository skillRepository;

    // Mappers for DTO to Entity conversion
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final ExperienceMapper experienceMapper;
    private final QualificationMapper qualificationMapper;
    private final TestimonialMapper testimonialMapper;
    private final UserSkillMapper userSkillMapper;


    @Override
    @Transactional
    public void restoreFromBackup(PortfolioBackupDto backupData) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        log.info("Initiating restore for user: {}", currentUser.getEmail());
        performRestore(currentUser, backupData);
    }

    @Override
    @Transactional
    public void restoreUserFromBackup(User targetUser, PortfolioBackupDto backupData) {
        log.info("Admin initiating restore for user: {}", targetUser.getEmail());
        performRestore(targetUser, backupData);
    }

    @Override
    @Transactional
    public void restoreSystemFromBackup(List<UserFullBackupDto> systemBackupData) {
        log.warn("Initiating SYSTEM-WIDE restore. This is a destructive operation.");
        wipeAllData(); // Start from a clean slate

        for (UserFullBackupDto userBackup : systemBackupData) {
            UserDto userDto = userBackup.getUser();
            PortfolioBackupDto portfolioDto = userBackup.getPortfolio();

            log.info("Restoring user: {}", userDto.getEmail());
            // Find existing user or create a new one from the backup.
            User restoredUser = userRepository.findByEmail(userDto.getEmail()).orElseGet(() -> {
                log.warn("User {} not found. Creating new user from backup. PASSWORD WILL NOT BE SET.", userDto.getEmail());
                User newUser = new User();
                newUser.setUuid(userDto.getId());
                newUser.setFirstName(userDto.getFirstName());
                newUser.setLastName(userDto.getLastName());
                newUser.setEmail(userDto.getEmail());
                newUser.setSlug(userDto.getSlug());
                newUser.setProfileImageUrl(userDto.getProfileImageUrl());
                newUser.setRoles(userDto.getRoles());
                newUser.setActive(userDto.isActive());
                newUser.setProvider(userDto.getProvider());
                newUser.setTermsAcceptedAt(userDto.getTermsAcceptedAt());
                newUser.setTermsVersion(userDto.getTermsVersion());
                // Password is intentionally not set. An admin must intervene manually post-restore.
                return userRepository.save(newUser);
            });

            // Restore the portfolio data for this user
            performRestore(restoredUser, portfolioDto);
        }
        log.info("System-wide restore completed.");
    }

    @Override
    @Transactional
    public void wipeAllData() {
        log.warn("Wiping all portfolio and user data from the database.");
        // Order is important to respect foreign key constraints
        userSkillRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        experienceRepository.deleteAllInBatch();
        qualificationRepository.deleteAllInBatch();
        testimonialRepository.deleteAllInBatch();
        portfolioProfileRepository.deleteAllInBatch();
        // Users and Skills are top-level
        userRepository.deleteAllInBatch();
        skillRepository.deleteAllInBatch();
        log.info("All data wiped successfully.");
    }

    /**
     * The core, reusable logic for restoring a single user's portfolio data.
     *
     * @param user       The user entity to restore data for.
     * @param backupData The DTO containing the portfolio data.
     */
    private void performRestore(User user, PortfolioBackupDto backupData) {
        // 1. Clear existing portfolio data for the user to ensure a clean restore
        // FIX: After deleting entities from the database, we must also clear the in-memory
        // collections on the managed User entity. This prevents Hibernate from having a stale
        // view of the collections, which can cause data integrity errors during the restore.
        if (!user.getUserSkills().isEmpty()) {
            userSkillRepository.deleteAll(user.getUserSkills());
            user.getUserSkills().clear();
        }
        if (!user.getProjects().isEmpty()) {
            projectRepository.deleteAll(user.getProjects());
            user.getProjects().clear();
        }
        if (!user.getExperiences().isEmpty()) {
            experienceRepository.deleteAll(user.getExperiences());
            user.getExperiences().clear();
        }
        qualificationRepository.deleteAll(user.getQualifications());
        testimonialRepository.deleteAll(user.getTestimonials());
        user.getQualifications().clear();
        user.getTestimonials().clear();

        if (user.getPortfolioProfile() != null) {
            // FIX: Explicitly break the association from the User side before deleting the profile.
            // This prevents a DataIntegrityViolationException caused by the persistence context
            // still thinking the user is linked to a profile when trying to save a new one.
            PortfolioProfile profileToDelete = user.getPortfolioProfile();
            user.setPortfolioProfile(null);
            portfolioProfileRepository.delete(profileToDelete);
        }

        // FIX: Force a flush to execute all pending DELETE statements before we start INSERTing.
        // This synchronizes the persistence context with the database, resolving the
        // DataIntegrityViolationException.
        entityManager.flush();

        // 2. Restore Portfolio Profile
        if (backupData.getProfile() != null) {
            // FIX: Use the correct mapper method to create an entity from the backup DTO.
            PortfolioProfile profile = portfolioProfileMapper.toEntityFromDto(backupData.getProfile(), user);
            user.setPortfolioProfile(profile); // Maintain bidirectional consistency for the managed entity
            portfolioProfileRepository.save(profile);
        }

        // 3. Restore Skills
        // FIX: Use a case-insensitive TreeMap for the skill cache. This prevents duplicate
        // skill creation attempts if the backup data has different casing (e.g., "java" vs "Java")
        // than what's already in the database, which often has a case-insensitive unique constraint.
        Map<String, Skill> skillCache = skillRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Skill::getName,
                        Function.identity(),
                        (existing, replacement) -> existing, // In case of case-insensitive duplicates, keep the first one.
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                ));

        List<UserSkill> restoredUserSkills = backupData.getSkills().stream().map(userSkillDto -> {
            Skill skill = skillCache.computeIfAbsent(userSkillDto.getName(), name -> {
                Skill newSkill = new Skill();
                newSkill.setName(name);
                newSkill.setCategory(userSkillDto.getCategory());
                newSkill.setIcon(userSkillDto.getIcon());
                // A new global skill created during a user restore should not have a description
                // set from a user-specific context. It can be null and updated by an admin later.
                return skillRepository.save(newSkill);
            });
            UserSkill userSkill = userSkillMapper.toEntity(userSkillDto);
            userSkill.setUser(user);
            userSkill.setSkill(skill);

            // FIX: All skills restored from a backup should default to being visible.
            userSkill.setVisible(true);

            // If the user-specific description from the backup is empty,
            // fall back to using the description from the global skill pool.
            if (!StringUtils.hasText(userSkill.getDescription())) {
                userSkill.setDescription(skill.getDescription());
            }
            return userSkill;
        }).collect(Collectors.toList());
        userSkillRepository.saveAll(restoredUserSkills);

        // Create a lookup map for the newly saved skills
        Map<String, Skill> restoredSkillMap = restoredUserSkills.stream()
                .map(UserSkill::getSkill)
                .distinct()
                .collect(Collectors.toMap(Skill::getName, Function.identity()));

        // 4. Restore Projects, Experiences, etc., using the restored skills
        if (backupData.getProjects() != null) {
            List<Project> projects = backupData.getProjects().stream().map(dto -> {
                // FIX: Use the correct mapper method and pass the user context.
                Project entity = projectMapper.toEntityFromDto(dto, user);
                Set<Skill> skills = dto.getSkills().stream()
                        .map(skillDto -> restoredSkillMap.get(skillDto.getName()))
                        .filter(Objects::nonNull) // Add null check for safety
                        .collect(Collectors.toSet());
                entity.setSkills(skills);
                return entity;
            }).collect(Collectors.toList());
            projectRepository.saveAll(projects);
        }

        if (backupData.getExperiences() != null) {
            List<Experience> experiences = backupData.getExperiences().stream().map(dto -> {
                // FIX: Use the correct mapper method and pass the user context.
                Experience entity = experienceMapper.toEntityFromDto(dto, user);
                Set<Skill> skills = dto.getSkills().stream()
                        .map(skillDto -> restoredSkillMap.get(skillDto.getName()))
                        .filter(Objects::nonNull) // Add null check for safety
                        .collect(Collectors.toSet());
                entity.setSkills(skills);
                return entity;
            }).collect(Collectors.toList());
            experienceRepository.saveAll(experiences);
        }

        if (backupData.getQualifications() != null) {
            List<Qualification> qualifications = backupData.getQualifications().stream().map(dto -> {
                // FIX: Use the correct mapper method and pass the user context.
                return qualificationMapper.toEntityFromDto(dto, user);
            }).collect(Collectors.toList());
            qualificationRepository.saveAll(qualifications);
        }

        if (backupData.getTestimonials() != null) {
            List<Testimonial> testimonials = backupData.getTestimonials().stream().map(dto -> {
                // FIX: Use the correct mapper method and pass the user context.
                return testimonialMapper.toEntityFromDto(dto, user);
            }).collect(Collectors.toList());
            testimonialRepository.saveAll(testimonials);
        }
    }
}