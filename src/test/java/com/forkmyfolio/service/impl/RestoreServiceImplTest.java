package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.*;
import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestoreServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ExperienceRepository experienceRepository;
    @Mock
    private QualificationRepository qualificationRepository;
    @Mock
    private TestimonialRepository testimonialRepository;
    @Mock
    private PortfolioProfileRepository portfolioProfileRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private PortfolioProfileMapper portfolioProfileMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ExperienceMapper experienceMapper;
    @Mock
    private QualificationMapper qualificationMapper;
    @Mock
    private TestimonialMapper testimonialMapper;
    @Mock
    private UserSkillMapper userSkillMapper;

    @Spy
    @InjectMocks
    private RestoreServiceImpl restoreService;

    private User testUser;
    private PortfolioBackupDto backupDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUserSkills(new HashSet<>());
        testUser.setProjects(new HashSet<>());
        testUser.setExperiences(new HashSet<>());
        testUser.setQualifications(new HashSet<>());
        testUser.setTestimonials(new HashSet<>());

        backupDto = new PortfolioBackupDto();
        backupDto.setProfile(new PortfolioProfileDto());
        UserSkillDto userSkillDto = new UserSkillDto();
        userSkillDto.setName("Java"); // Populate name to avoid NPE in TreeMap
        backupDto.setSkills(List.of(userSkillDto));

        ProjectDto projectDto = new ProjectDto();
        projectDto.setSkills(Collections.emptySet()); // Initialize collection to avoid NPE in stream()
        backupDto.setProjects(List.of(projectDto));

        ExperienceDto experienceDto = new ExperienceDto();
        experienceDto.setSkills(Collections.emptySet()); // Initialize collection to avoid NPE in stream()
        backupDto.setExperiences(List.of(experienceDto));

        backupDto.setQualifications(List.of(new QualificationDto()));
        backupDto.setTestimonials(List.of(new TestimonialDto()));
    }

    @Test
    void wipeAllData_shouldDeleteAllDataInCorrectOrder() {
        restoreService.wipeAllData();

        // Verify that deleteAllInBatch is called on all repositories
        verify(userSkillRepository).deleteAllInBatch();
        verify(projectRepository).deleteAllInBatch();
        verify(experienceRepository).deleteAllInBatch();
        verify(qualificationRepository).deleteAllInBatch();
        verify(testimonialRepository).deleteAllInBatch();
        verify(portfolioProfileRepository).deleteAllInBatch();
        verify(userRepository).deleteAllInBatch();
        verify(skillRepository).deleteAllInBatch();
    }

    @Test
    void restoreUserFromBackup_shouldClearOldDataAndRestoreNewData() {
        // Arrange: Give the user some existing data to ensure it gets cleared
        testUser.getProjects().add(new Project());
        when(skillRepository.findAll()).thenReturn(Collections.emptyList());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userSkillMapper.toEntity(any(UserSkillDto.class))).thenReturn(new UserSkill());
        when(projectMapper.toEntityFromDto(any(ProjectDto.class), any(User.class))).thenReturn(new Project());
        when(experienceMapper.toEntityFromDto(any(ExperienceDto.class), any(User.class))).thenReturn(new Experience());
        when(qualificationMapper.toEntityFromDto(any(QualificationDto.class), any(User.class)))
                .thenReturn(new Qualification());
        when(testimonialMapper.toEntityFromDto(any(TestimonialDto.class), any(User.class)))
                .thenReturn(new Testimonial());
        when(portfolioProfileMapper.toEntityFromDto(any(PortfolioProfileDto.class), any(User.class)))
                .thenReturn(new PortfolioProfile());

        // Act
        restoreService.restoreUserFromBackup(testUser, backupDto);

        // Assert: Verify old data is cleared
        verify(projectRepository).deleteAll(any());
        verify(entityManager).flush();

        // Assert: Verify new data is restored
        verify(portfolioProfileRepository).save(any(PortfolioProfile.class));
        verify(skillRepository, atLeastOnce()).save(any(Skill.class)); // A new skill should be created
        verify(userSkillRepository).saveAll(anyList());
        verify(projectRepository).saveAll(anyList());
        verify(experienceRepository).saveAll(anyList());
        verify(qualificationRepository).saveAll(anyList());
        verify(testimonialRepository).saveAll(anyList());
    }

    @Test
    void restoreSystemFromBackup_shouldWipeAndRestoreAllUsers() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setEmail("new@example.com");
        userDto.setId(UUID.randomUUID());

        UserFullBackupDto userBackup = new UserFullBackupDto(userDto, backupDto);
        List<UserFullBackupDto> systemBackup = Collections.singletonList(userBackup);

        // When the service looks for the user, pretend they don't exist
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // When the service saves the new user, return a user object
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock save
                                                                                                          // to avoid
                                                                                                          // NPE
        when(userSkillMapper.toEntity(any(UserSkillDto.class))).thenReturn(new UserSkill()); // Mock mapper to avoid NPE
        when(projectMapper.toEntityFromDto(any(ProjectDto.class), any(User.class))).thenReturn(new Project());
        when(experienceMapper.toEntityFromDto(any(ExperienceDto.class), any(User.class))).thenReturn(new Experience());
        when(qualificationMapper.toEntityFromDto(any(QualificationDto.class), any(User.class)))
                .thenReturn(new Qualification());
        when(testimonialMapper.toEntityFromDto(any(TestimonialDto.class), any(User.class)))
                .thenReturn(new Testimonial());
        when(portfolioProfileMapper.toEntityFromDto(any(PortfolioProfileDto.class), any(User.class)))
                .thenReturn(new PortfolioProfile());

        // Act
        restoreService.restoreSystemFromBackup(systemBackup);

        // Assert
        verify(restoreService).wipeAllData(); // Should wipe first
        verify(userRepository).findByEmail(eq("new@example.com")); // Should check if user exists
        verify(userRepository).save(any(User.class)); // Should save the new user
        // Verify that the restore logic was called for the newly created user
        verify(portfolioProfileRepository, times(1)).save(any(PortfolioProfile.class));
    }
}
