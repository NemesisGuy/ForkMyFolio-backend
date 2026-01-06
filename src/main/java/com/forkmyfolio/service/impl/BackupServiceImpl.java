package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.service.BackupService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

        private final PortfolioProfileMapper portfolioProfileMapper;
        private final ProjectMapper projectMapper;
        private final ExperienceMapper experienceMapper;
        private final TestimonialMapper testimonialMapper;
        private final QualificationMapper qualificationMapper;
        private final UserSkillMapper userSkillMapper;
        private final UserMapper userMapper;
        private final UserService userService;
        private final com.forkmyfolio.repository.UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public List<UserFullBackupDto> createFullSystemBackup() {
                // Fetch users using the simplified query (User + Profile + Roles)
                List<User> allUsers = userService.getAllUsersWithPortfolioData();
                List<UserFullBackupDto> systemBackupData = new ArrayList<>();

                for (User user : allUsers) {
                        // This method "touches" collections. Because we are @Transactional,
                        // Hibernate will trigger batch fetches for these collections.
                        PortfolioBackupDto portfolioBackup = createBackupDtoForUser(user);
                        UserDto userDto = userMapper.toDto(user);
                        systemBackupData.add(new UserFullBackupDto(userDto, portfolioBackup));
                }

                return systemBackupData;
        }

        @Override
        @Transactional(readOnly = true)
        public PortfolioBackupDto createBackupDtoForUser(User user) {
                PortfolioBackupDto backupDto = new PortfolioBackupDto();

                // Create the lookup map that the mappers need to build complete DTOs.
                // The key is the global Skill UUID, and the value is the full UserSkill entity.
                Map<UUID, UserSkill> userSkillLookup = user.getUserSkills().stream()
                                .collect(Collectors.toMap(
                                                userSkill -> userSkill.getSkill().getUuid(),
                                                userSkill -> userSkill, // The value is the UserSkill itself
                                                (existing, replacement) -> existing // In case of duplicates, keep the
                                                                                    // existing one
                                ));

                if (user.getPortfolioProfile() != null) {
                        backupDto.setProfile(portfolioProfileMapper.toDto(user.getPortfolioProfile()));
                }

                // The mappers now correctly handle the inclusion of all skill details,
                // including user-specific ones.
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

        @Override
        @Transactional(readOnly = true)
        public PortfolioBackupDto createBackupDtoForUser(java.util.UUID userUuid) {
                User user = userRepository.findByUuid(userUuid)
                                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                                                "User not found with UUID: " + userUuid));
                return createBackupDtoForUser(user);
        }
}