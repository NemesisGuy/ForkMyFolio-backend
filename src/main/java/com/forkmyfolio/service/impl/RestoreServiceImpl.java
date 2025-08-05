package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.dto.response.UserSkillDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.model.enums.SkillLevel;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreServiceImpl implements RestoreService {

    //<editor-fold desc="Repositories, Mappers, Services">
    private final ProjectRepository projectRepository;
    private final UserSkillRepository userSkillRepository;
    private final ExperienceRepository experienceRepository;
    private final TestimonialRepository testimonialRepository;
    private final QualificationRepository qualificationRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final SkillRepository skillRepository;

    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;

    private final UserService userService;
    //</editor-fold>

    @Override
    @Transactional
    public void restoreFromBackup(PortfolioBackupDto backupDto) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        performRestore(backupDto, currentUser);
    }

    @Override
    @Transactional
    public void restoreForSpecificUser(PortfolioBackupDto portfolioDto, User user) {
        performRestore(portfolioDto, user);
    }

    private void performRestore(PortfolioBackupDto backupDto, User user) {
        log.info("Starting restore for user ID: {}", user.getId());
        clearExistingData(user);

        // Stage 1: Find or create all necessary Skill entities and build a master cache.
        Map<String, Skill> skillNameCache = buildAndReconcileSkillCache(backupDto);
        log.info("Skill cache reconciliation complete. Cache contains {} entries.", skillNameCache.size());

        // Stage 2: Restore all portfolio items, using the master cache to link skills.
        restoreUserSkillRelationships(backupDto, user, skillNameCache);
        restoreProjects(backupDto, user, skillNameCache);
        restoreExperiences(backupDto, user, skillNameCache);
        restoreProfile(backupDto, user);
        restoreQualifications(backupDto, user);
        restoreTestimonials(backupDto, user);

        log.info("Completed restore for user ID: {}", user.getId());
    }

    private void restoreProfile(PortfolioBackupDto backupDto, User user) {
        if (backupDto.getProfile() != null) {
            PortfolioProfile profile = portfolioProfileRepository.findByUser(user)
                    .orElse(new PortfolioProfile());
            profile.setUser(user);
            portfolioProfileMapper.applyUpdateFromDto(backupDto.getProfile(), profile);
            portfolioProfileRepository.save(profile);
            log.info("Restored profile for user ID: {}", user.getId());
        }
    }

    private void restoreProjects(PortfolioBackupDto backupDto, User user, Map<String, Skill> skillNameCache) {
        if (backupDto.getProjects() != null) {
            List<Project> projectsToRestore = backupDto.getProjects().stream().map(projectDto -> {
                Project project = projectMapper.toEntityFromDto(projectDto, user);
                Set<Skill> skills = resolveSkills(projectDto.getSkills(), skillNameCache);
                project.setSkills(skills);
                return project;
            }).collect(Collectors.toList());
            projectRepository.saveAll(projectsToRestore);
            log.info("Restored {} projects for user ID: {}", projectsToRestore.size(), user.getId());
        }
    }

    private void restoreExperiences(PortfolioBackupDto backupDto, User user, Map<String, Skill> skillNameCache) {
        if (backupDto.getExperiences() != null) {
            List<Experience> experiencesToRestore = backupDto.getExperiences().stream().map(expDto -> {
                Experience experience = experienceMapper.toEntityFromDto(expDto, user);
                Set<Skill> skills = resolveSkills(expDto.getSkills(), skillNameCache);
                experience.setSkills(skills);
                return experience;
            }).collect(Collectors.toList());
            experienceRepository.saveAll(experiencesToRestore);
            log.info("Restored {} experiences for user ID: {}", experiencesToRestore.size(), user.getId());
        }
    }

    private void restoreQualifications(PortfolioBackupDto backupDto, User user) {
        if (backupDto.getQualifications() != null) {
            List<Qualification> qualifications = backupDto.getQualifications().stream()
                    .map(dto -> qualificationMapper.toEntityFromDto(dto, user))
                    .collect(Collectors.toList());
            qualificationRepository.saveAll(qualifications);
            log.info("Restored {} qualifications for user ID: {}", qualifications.size(), user.getId());
        }
    }

    private void restoreTestimonials(PortfolioBackupDto backupDto, User user) {
        if (backupDto.getTestimonials() != null) {
            List<Testimonial> testimonials = backupDto.getTestimonials().stream()
                    .map(dto -> testimonialMapper.toEntityFromDto(dto, user))
                    .collect(Collectors.toList());
            testimonialRepository.saveAll(testimonials);
            log.info("Restored {} testimonials for user ID: {}", testimonials.size(), user.getId());
        }
    }

    /**
     * Gathers ALL skills from the backup (top-level, projects, experiences), de-duplicates them by name
     * (keeping the one with the highest proficiency level), and creates or updates UserSkill relationships.
     */
    private void restoreUserSkillRelationships(PortfolioBackupDto backupDto, User user, Map<String, Skill> skillNameCache) {
        // 1. Gather all skills from all sources into a single, de-duplicated map,
        // keeping the entry with the highest proficiency level.
        Map<String, UserSkillDto> uniqueSkillsByName = new HashMap<>();

        Stream<UserSkillDto> skillsFromTopLevel = backupDto.getSkills() != null ? backupDto.getSkills().stream() : Stream.empty();
        Stream<UserSkillDto> skillsFromProjects = backupDto.getProjects() != null ?
                backupDto.getProjects().stream()
                        .filter(p -> p.getSkills() != null)
                        .flatMap(p -> p.getSkills().stream())
                        .map(this::skillDtoToUserSkillDto)
                : Stream.empty();
        Stream<UserSkillDto> skillsFromExperiences = backupDto.getExperiences() != null ?
                backupDto.getExperiences().stream()
                        .filter(e -> e.getSkills() != null)
                        .flatMap(e -> e.getSkills().stream())
                        .map(this::skillDtoToUserSkillDto)
                : Stream.empty();

        Stream.concat(skillsFromTopLevel, Stream.concat(skillsFromProjects, skillsFromExperiences))
                .filter(Objects::nonNull)
                .forEach(userSkillDto -> {
                    if (!StringUtils.hasText(userSkillDto.getName())) {
                        return;
                    }
                    String skillName = userSkillDto.getName().toLowerCase();
                    uniqueSkillsByName.compute(skillName, (name, existingDto) -> {
                        if (existingDto == null) {
                            return userSkillDto;
                        }
                        if (isNewLevelHigher(userSkillDto, existingDto)) {
                            log.info("De-duplicating skill '{}': Keeping new DTO with level {} over existing level {}.",
                                    userSkillDto.getName(), userSkillDto.getLevel(), existingDto.getLevel());
                            return userSkillDto;
                        } else {
                            log.info("De-duplicating skill '{}': Keeping existing DTO with level {} over new level {}.",
                                    existingDto.getName(), existingDto.getLevel(), userSkillDto.getLevel());
                            return existingDto;
                        }
                    });
                });

        if (uniqueSkillsByName.isEmpty()) {
            log.info("No UserSkills to restore for user ID: {}", user.getId());
            return;
        }

        log.info("De-duplicated all skills from backup to {} unique UserSkill entries.", uniqueSkillsByName.size());

        // 2. Fetch existing UserSkill records for the user to check for duplicates.
        List<UserSkill> existingUserSkills = userSkillRepository.findByUser(user);
        Map<Long, UserSkill> existingUserSkillsBySkillId = existingUserSkills.stream()
                .collect(Collectors.toMap(
                        userSkill -> userSkill.getSkill().getId(),
                        Function.identity(),
                        (existing, duplicate) -> {
                            log.warn("Duplicate UserSkill found for user ID {} and skill ID {}. Keeping first entry.", user.getId(), existing.getSkill().getId());
                            return existing;
                        }
                ));

        // 3. Create or update UserSkill entities from the de-duplicated map.
        List<UserSkill> skillsToSave = new ArrayList<>();
        List<UserSkill> skillsToUpdate = new ArrayList<>();

        uniqueSkillsByName.values().forEach(userSkillDto -> {
            Skill globalSkill = skillNameCache.get(userSkillDto.getName().toLowerCase());
            if (globalSkill == null) {
                log.error("CRITICAL: Skill '{}' was not found in the cache. This should not happen. Skipping UserSkill creation.", userSkillDto.getName());
                return;
            }

            UserSkill existingUserSkill = existingUserSkillsBySkillId.get(globalSkill.getId());
            if (existingUserSkill != null) {
                // Update existing UserSkill if level or description differs
                boolean needsUpdate = false;
                SkillLevel newLevel = userSkillDto.getLevel() != null ? userSkillDto.getLevel() : SkillLevel.BEGINNER;
                String newDescription = StringUtils.hasText(userSkillDto.getDescription()) ? userSkillDto.getDescription() : globalSkill.getDescription();

                if (newLevel != existingUserSkill.getLevel()) {
                    log.info("Updating UserSkill for user ID {} and skill '{}': Level from {} to {}.",
                            user.getId(), globalSkill.getName(), existingUserSkill.getLevel(), newLevel);
                    existingUserSkill.setLevel(newLevel);
                    needsUpdate = true;
                }
                if (!Objects.equals(newDescription, existingUserSkill.getDescription())) {
                    log.info("Updating UserSkill for user ID {} and skill '{}': Description updated.",
                            user.getId(), globalSkill.getName());
                    existingUserSkill.setDescription(newDescription);
                    needsUpdate = true;
                }
                if (needsUpdate) {
                    skillsToUpdate.add(existingUserSkill);
                }
            } else {
                // Create new UserSkill
                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(globalSkill);
                userSkill.setLevel(userSkillDto.getLevel() != null ? userSkillDto.getLevel() : SkillLevel.BEGINNER);
                userSkill.setVisible(true);
                userSkill.setDescription(StringUtils.hasText(userSkillDto.getDescription()) ? userSkillDto.getDescription() : globalSkill.getDescription());
                skillsToSave.add(userSkill);
            }
        });

        // 4. Save new UserSkills and update existing ones.
        if (!skillsToSave.isEmpty()) {
            log.info("Saving {} new UserSkill entries for user ID: {}", skillsToSave.size(), user.getId());
            userSkillRepository.saveAll(skillsToSave);
        }
        if (!skillsToUpdate.isEmpty()) {
            log.info("Updating {} existing UserSkill entries for user ID: {}", skillsToUpdate.size(), user.getId());
            userSkillRepository.saveAll(skillsToUpdate);
        }
    }

    /**
     * Builds a cache of Skill entities by name (lowercase).
     * It scans all skills in backup (projects, experiences, user skills),
     * fetches existing Skills from DB by name, creates missing skills,
     * and returns the map.
     */
    private Map<String, Skill> buildAndReconcileSkillCache(PortfolioBackupDto backupDto) {
        log.info("Reconciling skills from backup data...");

        // 1. Gather all unique skill DTOs from every part of the backup file.
        Map<String, SkillDto> allUniqueSkillDtosByName = new HashMap<>();

        Stream<SkillDto> skillsFromTopLevel = backupDto.getSkills() != null ?
                backupDto.getSkills().stream().map(this::userSkillDtoToSkillDto) : Stream.empty();
        Stream<SkillDto> skillsFromProjects = backupDto.getProjects() != null ?
                backupDto.getProjects().stream()
                        .filter(p -> p.getSkills() != null)
                        .flatMap(p -> p.getSkills().stream())
                : Stream.empty();
        Stream<SkillDto> skillsFromExperiences = backupDto.getExperiences() != null ?
                backupDto.getExperiences().stream()
                        .filter(e -> e.getSkills() != null)
                        .flatMap(e -> e.getSkills().stream())
                : Stream.empty();

        Stream.concat(skillsFromTopLevel, Stream.concat(skillsFromProjects, skillsFromExperiences))
                .filter(Objects::nonNull)
                .forEach(dto -> {
                    if (StringUtils.hasText(dto.getName())) {
                        allUniqueSkillDtosByName.putIfAbsent(dto.getName().toLowerCase(), dto);
                    }
                });

        if (allUniqueSkillDtosByName.isEmpty()) {
            log.info("No valid skills found in backup to reconcile.");
            return Collections.emptyMap();
        }

        Set<String> requiredNames = allUniqueSkillDtosByName.keySet();
        log.info("Found {} unique skill names in backup", requiredNames.size());

        // 2. Find all existing skills by name from the DB.
        Map<String, Skill> skillCache = skillRepository.findByNameIn(requiredNames).stream()
                .collect(Collectors.toMap(s -> s.getName().toLowerCase(), Function.identity()));

        // 3. Identify and create missing skills.
        List<Skill> skillsToCreate = allUniqueSkillDtosByName.entrySet().stream()
                .filter(entry -> !skillCache.containsKey(entry.getKey()))
                .map(entry -> {
                    SkillDto dto = entry.getValue();
                    log.info("Queuing new skill for creation: name='{}', UUID={}", dto.getName(), dto.getSkillId());
                    Skill newSkill = new Skill();
                    newSkill.setUuid(dto.getSkillId() != null ? dto.getSkillId() : UUID.randomUUID());
                    newSkill.setName(dto.getName());
                    newSkill.setVisible(true);
                    newSkill.setCategory(dto.getCategory());
                    newSkill.setIcon(dto.getIcon());
                    newSkill.setDescription(dto.getDescription());
                    return newSkill;
                })
                .collect(Collectors.toList());

        if (!skillsToCreate.isEmpty()) {
            List<Skill> newlyCreatedSkills = skillRepository.saveAll(skillsToCreate);
            log.info("Created {} new skills in the database.", newlyCreatedSkills.size());
            newlyCreatedSkills.forEach(s -> skillCache.put(s.getName().toLowerCase(), s));
        }

        return skillCache;
    }

    /**
     * Helper to convert a SkillDto to a UserSkillDto for unified processing.
     */
    private UserSkillDto skillDtoToUserSkillDto(SkillDto skillDto) {
        if (skillDto == null) return null;
        UserSkillDto userSkillDto = new UserSkillDto();
        userSkillDto.setSkillId(skillDto.getSkillId());
        userSkillDto.setName(skillDto.getName());
        userSkillDto.setLevel(skillDto.getLevel());
        userSkillDto.setDescription(skillDto.getDescription());
        return userSkillDto;
    }

    /**
     * Helper to convert a UserSkillDto to a SkillDto for unified processing.
     */
    private SkillDto userSkillDtoToSkillDto(UserSkillDto userSkillDto) {
        if (userSkillDto == null) return null;
        SkillDto skillDto = new SkillDto();
        skillDto.setSkillId(userSkillDto.getSkillId());
        skillDto.setName(userSkillDto.getName());
        skillDto.setLevel(userSkillDto.getLevel());
        skillDto.setDescription(userSkillDto.getDescription());
        return skillDto;
    }

    /**
     * Compares the proficiency level of a new DTO to an existing one.
     */
    private boolean isNewLevelHigher(UserSkillDto newDto, UserSkillDto existingDto) {
        int newLevel = getLevelValue(newDto.getLevel());
        int existingLevel = getLevelValue(existingDto.getLevel());
        return newLevel > existingLevel;
    }

    private int getLevelValue(SkillLevel level) {
        if (level == null) return 0;
        return level.ordinal(); // Assumes enum order is from lowest to highest proficiency
    }

    /**
     * Resolve SkillDto list into persisted Skill entities using the master skill cache.
     */
    private Set<Skill> resolveSkills(Set<SkillDto> skillDtos, Map<String, Skill> skillNameCache) {
        if (skillDtos == null || skillDtos.isEmpty()) {
            return Collections.emptySet();
        }
        return skillDtos.stream()
                .filter(dto -> dto != null && StringUtils.hasText(dto.getName()))
                .map(dto -> skillNameCache.get(dto.getName().toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void clearExistingData(User user) {
        log.info("Clearing existing data for user ID: {}", user.getId());

        // Delete in an order that respects foreign key constraints
        projectRepository.deleteAll(projectRepository.findByUser(user));
        experienceRepository.deleteAll(experienceRepository.findByUser(user));
        testimonialRepository.deleteAll(testimonialRepository.findByUser(user));
        qualificationRepository.deleteAll(qualificationRepository.findByUser(user));
        userSkillRepository.deleteAll(userSkillRepository.findByUser(user));

        log.info("Successfully cleared portfolio data for user ID: {}", user.getId());
    }
}