package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.CreateSkillRequest;
import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    private User user;
    private Skill skill1;
    private SkillDto skillDto1;
    private CreateSkillRequest createSkillRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRoles(Set.of(Role.ADMIN)); // Assume admin for creation/deletion tests

        LocalDateTime now = LocalDateTime.now();
        skill1 = new Skill();
        skill1.setId(1L);
        skill1.setName("Java");
        skill1.setLevel(Skill.SkillLevel.EXPERT);
        skill1.setUser(user);
        skill1.setCreatedAt(now.minusDays(1));
        skill1.setUpdatedAt(now);

        skillDto1 = new SkillDto(
            skill1.getId(), skill1.getName(), skill1.getLevel(),
            user.getId(), skill1.getCreatedAt(), skill1.getUpdatedAt()
        );

        createSkillRequest = new CreateSkillRequest("Python", Skill.SkillLevel.INTERMEDIATE);
    }

    @Test
    void getAllSkills_shouldReturnListOfSkillDtos() {
        when(skillRepository.findAll()).thenReturn(Collections.singletonList(skill1));
        List<SkillDto> result = skillService.getAllSkills();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(skill1.getName(), result.get(0).getName());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkillsByUserId_shouldReturnUserSkills() {
        when(skillRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(skill1));
        List<SkillDto> result = skillService.getAllSkillsByUserId(user.getId());
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(skill1.getName(), result.get(0).getName());
        assertEquals(user.getId(), result.get(0).getUserId());
        verify(skillRepository).findByUserId(user.getId());
    }


    @Test
    void findSkillEntityById_whenExists_shouldReturnSkill() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill1));
        Skill found = skillService.findSkillEntityById(1L);
        assertNotNull(found);
        assertEquals(skill1.getName(), found.getName());
    }

    @Test
    void findSkillEntityById_whenNotExists_shouldThrowResourceNotFoundException() {
        when(skillRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillService.findSkillEntityById(2L));
    }

    @Test
    void getSkillById_whenExists_shouldReturnSkillDto() {
         when(skillRepository.findById(1L)).thenReturn(Optional.of(skill1));
        SkillDto resultDto = skillService.getSkillById(1L);
        assertNotNull(resultDto);
        assertEquals(skill1.getName(), resultDto.getName());
    }

    @Test
    void createSkill_shouldSaveAndReturnSkillDto() {
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill s = invocation.getArgument(0);
            s.setId(2L); // Simulate DB assigning an ID
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        SkillDto result = skillService.createSkill(createSkillRequest, user);

        assertNotNull(result);
        assertEquals(createSkillRequest.getName(), result.getName());
        assertEquals(createSkillRequest.getLevel(), result.getLevel());
        assertEquals(user.getId(), result.getUserId());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void deleteSkill_whenSkillExists_shouldCallRepositoryDelete() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill1));
        doNothing().when(skillRepository).delete(skill1);

        skillService.deleteSkill(1L, user); // User passed for potential future auth checks

        verify(skillRepository).findById(1L);
        verify(skillRepository).delete(skill1);
    }

    @Test
    void deleteSkill_whenSkillNotExists_shouldThrowResourceNotFoundException() {
        when(skillRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillService.deleteSkill(2L, user));
        verify(skillRepository, never()).delete(any(Skill.class));
    }

    @Test
    void convertToDto_shouldCorrectlyMapFields() {
        SkillDto dto = skillService.convertToDto(skill1);
        assertEquals(skill1.getId(), dto.getId());
        assertEquals(skill1.getName(), dto.getName());
        assertEquals(skill1.getLevel(), dto.getLevel());
        assertEquals(skill1.getUser().getId(), dto.getUserId());
        assertEquals(skill1.getCreatedAt(), dto.getCreatedAt());
        assertEquals(skill1.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void convertToDto_withNullSkill_shouldReturnNull() {
        assertNull(skillService.convertToDto(null));
    }

    @Test
    void convertToDto_withSkillHavingNullUser_shouldHandleNullUserId() {
        skill1.setUser(null);
        SkillDto dto = skillService.convertToDto(skill1);
        assertNotNull(dto);
        assertNull(dto.getUserId());
    }

    @Test
    void convertCreateRequestToEntity_shouldCorrectlyMapFields() {
        Skill entity = skillService.convertCreateRequestToEntity(createSkillRequest, user);
        assertEquals(createSkillRequest.getName(), entity.getName());
        assertEquals(createSkillRequest.getLevel(), entity.getLevel());
        assertEquals(user, entity.getUser());
        assertNull(entity.getId()); // ID should be null before saving
        assertNull(entity.getCreatedAt()); // Timestamps handled by Hibernate
        assertNull(entity.getUpdatedAt());
    }
}
