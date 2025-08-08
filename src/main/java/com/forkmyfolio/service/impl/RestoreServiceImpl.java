package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        userSkillRepository.deleteAll(user.getUserSkills());
        projectRepository.deleteAll(user.getProjects());
        experienceRepository.deleteAll(user.getExperiences());
        qualificationRepository.deleteAll(user.getQualifications());
        testimonialRepository.deleteAll(user.getTestimonials());
        if (user.getPortfolioProfile() != null) {
            portfolioProfileRepository.delete(user.getPortfolioProfile());
        }

        // 2. Restore Portfolio Profile
        if (backupData.getProfile() != null) {
            // FIX: Use the correct mapper method to create an entity from the backup DTO.
            PortfolioProfile profile = portfolioProfileMapper.toEntityFromDto(backupData.getProfile(), user);
            portfolioProfileRepository.save(profile);
        }

        // 3. Restore Skills
        Map<String, Skill> skillCache = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getName, Function.identity()));

        List<UserSkill> restoredUserSkills = backupData.getSkills().stream().map(userSkillDto -> {
            Skill skill = skillCache.computeIfAbsent(userSkillDto.getName(), name -> {
                Skill newSkill = new Skill();
                newSkill.setName(name);
                newSkill.setCategory(userSkillDto.getCategory());
                newSkill.setIcon(userSkillDto.getIcon());
                return skillRepository.save(newSkill);
            });
            UserSkill userSkill = userSkillMapper.toEntity(userSkillDto);
            userSkill.setUser(user);
            userSkill.setSkill(skill);
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
                        .map(restoredSkillMap::get)
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
                        .map(restoredSkillMap::get)
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