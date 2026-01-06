package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.*;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.model.enums.SkillLevel;
import com.forkmyfolio.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceImplTest {

    @Mock
    private PortfolioProfileMapper portfolioProfileMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ExperienceMapper experienceMapper;
    @Mock
    private TestimonialMapper testimonialMapper;
    @Mock
    private QualificationMapper qualificationMapper;
    @Mock
    private UserSkillMapper userSkillMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private BackupServiceImpl backupService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setPortfolioProfile(new PortfolioProfile());
        testUser.setProjects(Set.of(new Project()));

        // Correctly set up Skill and UserSkill
        Skill skill = new Skill();
        skill.setName("Java");

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(testUser);
        userSkill.setSkill(skill);
        userSkill.setLevel(SkillLevel.EXPERT);
        userSkill.setVisible(true);
        testUser.setUserSkills(Set.of(userSkill));

        testUser.setExperiences(Set.of(new Experience()));
        testUser.setTestimonials(Set.of(new Testimonial()));
        testUser.setQualifications(Set.of(new Qualification()));
    }

    @Test
    void createBackupDtoForUser_withFullData_shouldMapAllFields() {
        // Arrange
        when(portfolioProfileMapper.toDto(any(PortfolioProfile.class))).thenReturn(new PortfolioProfileDto());
        when(projectMapper.toDto(any(Project.class), anyMap())).thenReturn(new ProjectDto());
        when(userSkillMapper.toDtoList(any())).thenReturn(Collections.singletonList(new UserSkillDto()));
        when(experienceMapper.toDto(any(Experience.class), anyMap())).thenReturn(new ExperienceDto());
        when(testimonialMapper.toDto(any(Testimonial.class))).thenReturn(new TestimonialDto());
        when(qualificationMapper.toDto(any(Qualification.class))).thenReturn(new QualificationDto());

        // Act
        PortfolioBackupDto backupDto = backupService.createBackupDtoForUser(testUser);

        // Assert
        assertNotNull(backupDto);
        assertNotNull(backupDto.getProfile());
        assertEquals(1, backupDto.getProjects().size());
        assertEquals(1, backupDto.getSkills().size());
        assertEquals(1, backupDto.getExperiences().size());
        assertEquals(1, backupDto.getTestimonials().size());
        assertEquals(1, backupDto.getQualifications().size());

        verify(portfolioProfileMapper, times(1)).toDto(any(PortfolioProfile.class));
        verify(projectMapper, times(1)).toDto(any(Project.class), anyMap());
        verify(userSkillMapper, times(1)).toDtoList(any());
        verify(experienceMapper, times(1)).toDto(any(Experience.class), anyMap());
        verify(testimonialMapper, times(1)).toDto(any(Testimonial.class));
        verify(qualificationMapper, times(1)).toDto(any(Qualification.class));
    }

    @Test
    void createBackupDtoForUser_withEmptyData_shouldReturnEmptyDto() {
        // Arrange
        User emptyUser = new User();
        emptyUser.setPortfolioProfile(null); // Explicitly null profile

        // Act
        PortfolioBackupDto backupDto = backupService.createBackupDtoForUser(emptyUser);

        // Assert
        assertNotNull(backupDto);
        assertNull(backupDto.getProfile());
        assertNotNull(backupDto.getProjects());
        assertTrue(backupDto.getProjects().isEmpty());
        assertNotNull(backupDto.getSkills());
        assertTrue(backupDto.getSkills().isEmpty());
        assertNotNull(backupDto.getExperiences());
        assertTrue(backupDto.getExperiences().isEmpty());
        assertNotNull(backupDto.getTestimonials());
        assertTrue(backupDto.getTestimonials().isEmpty());
        assertNotNull(backupDto.getQualifications());
        assertTrue(backupDto.getQualifications().isEmpty());
    }

    @Test
    void createFullSystemBackup_shouldReturnMappedList() {
        // Arrange
        when(userService.getAllUsersWithPortfolioData()).thenReturn(Collections.singletonList(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());

        // Mock the internal mappings
        when(portfolioProfileMapper.toDto(any())).thenReturn(new PortfolioProfileDto());
        when(projectMapper.toDto(any(), anyMap())).thenReturn(new ProjectDto());
        when(userSkillMapper.toDtoList(any())).thenReturn(Collections.singletonList(new UserSkillDto()));

        // Act
        java.util.List<UserFullBackupDto> result = backupService.createFullSystemBackup();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).getAllUsersWithPortfolioData();
        verify(userMapper).toDto(any(User.class));
    }
}
