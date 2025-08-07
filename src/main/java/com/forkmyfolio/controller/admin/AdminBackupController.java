package com.forkmyfolio.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.aop.SkipApiResponseWrapper;
import com.forkmyfolio.dto.backup.BackupFileDto;
import com.forkmyfolio.dto.backup.BackupMetaDto;
import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.BackupValidationService;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/backup")
@Tag(name = "Admin: System Backup & Restore", description = "Endpoints for administrators to perform system-wide backups and restores.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AdminBackupController {

    // Services for validation and restore logic
    private final BackupValidationService backupValidationService;
    private final RestoreService restoreService;
    private final UserService userService;

    // Repositories for data access and wiping
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

    // Mappers for DTO conversion
    private final UserMapper userMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;
    private final UserSkillMapper userSkillMapper;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @GetMapping
    @Operation(summary = "Create a full system backup", description = "Downloads a versioned JSON file containing all data for all users in the system.")
    @SkipApiResponseWrapper
    public ResponseEntity<byte[]> downloadSystemBackup() throws IOException {
        log.info("Admin request for system-wide backup initiated.");
        // FIX: Use the service method that eagerly fetches all users with their full portfolio data.
        List<User> allUsers = userService.getAllUsersWithPortfolioData();
        List<UserFullBackupDto> systemBackupData = new ArrayList<>();

        for (User user : allUsers) {
            PortfolioBackupDto portfolioBackup = createBackupDtoForUser(user);
            UserDto userDto = userMapper.toDto(user);
            systemBackupData.add(new UserFullBackupDto(userDto, portfolioBackup));
        }

        log.info("Successfully generated backup data for {} users.", systemBackupData.size());

        BackupMetaDto meta = BackupMetaDto.builder()
                .version(appVersion)
                .exportedAt(ZonedDateTime.now())
                .type("system_backup")
                .compatibility(BackupMetaDto.Compatibility.builder()
                        .minSupportedVersion("2.0.0")
                        .maxSupportedVersion("2.x")
                        .build())
                .system("ForkMyFolio")
                .build();

        BackupFileDto<List<UserFullBackupDto>> backupFile = new BackupFileDto<>(meta, systemBackupData);

        String filename = String.format("forkmyfolio-system-backup-%s.json", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        byte[] jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backupFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok().headers(headers).body(jsonContent);
    }

    @PostMapping("/restore")
    @Operation(summary = "Restore the entire system from a backup", description = "Upload a system backup file. THIS IS A DESTRUCTIVE OPERATION and will wipe all existing data before restoring.")
    @Transactional // A single transaction for the entire restore process
    public ResponseEntity<Void> restoreSystemFromBackup(@RequestParam("file") MultipartFile file) throws IOException {
        log.warn("Admin request for system-wide restore initiated. THIS IS A DESTRUCTIVE OPERATION.");
        if (file.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(file.getContentType()))) {
            throw new IllegalArgumentException("Invalid file. Please upload a valid JSON backup file.");
        }

        BackupFileDto<List<UserFullBackupDto>> backupFile = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {});

        backupValidationService.validateBackup(backupFile.getMeta(), "system_backup");

        List<UserFullBackupDto> backupData = backupFile.getData();

        // 1. Wipe all existing data
        wipeAllData();

        // 2. Restore all users and their portfolios
        for (UserFullBackupDto userBackup : backupData) {
            UserDto userDto = userBackup.getUser();
            PortfolioBackupDto portfolioDto = userBackup.getPortfolio();

            User user = new User();
            user.setUuid(userDto.getId());
            user.setEmail(userDto.getEmail());
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setSlug(userDto.getSlug());
            user.setProfileImageUrl(userDto.getProfileImageUrl());
            user.setRoles(userDto.getRoles());
            user.setActive(userDto.isActive());
            user.setProvider(userDto.getProvider());
            user.setProviderId(userDto.getProviderId());
//            user.setEmailVerified(userDto.isEmailVerified());
            user.setPassword(passwordEncoder.encode("restored-password-" + UUID.randomUUID()));
            User restoredUser = userRepository.save(user);

            restoreService.restoreForSpecificUser(portfolioDto, restoredUser);
        }
        log.warn("System restore completed successfully. {} users restored.", backupData.size());

        return ResponseEntity.noContent().build();
    }

    private void wipeAllData() {
        log.info("Wiping all existing portfolio and user data...");
        // Order is critical to avoid foreign key constraint violations.
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

    private PortfolioBackupDto createBackupDtoForUser(User user) {
        PortfolioBackupDto backupDto = new PortfolioBackupDto();

        // Create the lookup map that the mappers need to build complete DTOs.
        // The key is the global Skill UUID, and the value is the full UserSkill entity.
        Map<UUID, UserSkill> userSkillLookup = user.getUserSkills().stream()
                .collect(Collectors.toMap(
                        userSkill -> userSkill.getSkill().getUuid(),
                        userSkill -> userSkill, // The value is the UserSkill itself
                        (existing, replacement) -> existing // In case of duplicates, keep the existing one
                ));

        if (user.getPortfolioProfile() != null) {
            backupDto.setProfile(portfolioProfileMapper.toDto(user.getPortfolioProfile()));
        }

        // The mappers now correctly handle the inclusion of all skill details, including user-specific ones.
        backupDto.setProjects(user.getProjects().stream()
                .map(project -> projectMapper.toDto(project, userSkillLookup))
                .collect(Collectors.toList()));

        backupDto.setSkills(userSkillMapper.toDtoList(new ArrayList<>(user.getUserSkills())));

        backupDto.setExperiences(user.getExperiences().stream()
                .map(experience -> experienceMapper.toDto(experience, userSkillLookup))
                .collect(Collectors.toList()));

        backupDto.setTestimonials(user.getTestimonials().stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList()));

        backupDto.setQualifications(user.getQualifications().stream()
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList()));

        return backupDto;
    }
}