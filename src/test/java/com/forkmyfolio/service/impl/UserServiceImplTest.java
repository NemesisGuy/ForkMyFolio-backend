package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.request.RegisterRequest;
import com.forkmyfolio.exception.EmailAlreadyExistsException;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "test@example.com",
                "Test",
                "User",
                "password123",
                "http://example.com/image.png",
                Set.of(Role.USER)
        );

        user = new User();
        user.setId(1L);
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword("encodedPassword"); // Assume it's encoded
        user.setProfileImageUrl(registerRequest.getProfileImageUrl());
        user.setRoles(registerRequest.getRoles());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void registerUser_whenEmailNotExists_shouldSaveAndReturnUser() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(registerRequest);

        assertNotNull(savedUser);
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(Role.USER));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_whenEmailExists_shouldThrowEmailAlreadyExistsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.registerUser(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_withNullRoles_shouldAssignDefaultUserRole() {
        registerRequest.setRoles(null);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // Capture the user argument passed to save
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // Set ID and timestamps as if DB did it, for consistent return object
            userToSave.setId(2L);
            userToSave.setCreatedAt(LocalDateTime.now());
            userToSave.setUpdatedAt(LocalDateTime.now());
            return userToSave;
        });

        User savedUser = userService.registerUser(registerRequest);

        assertNotNull(savedUser);
        assertTrue(savedUser.getRoles().contains(Role.USER));
        assertEquals(1, savedUser.getRoles().size());
        verify(userRepository).save(any(User.class));
    }


    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        User foundUser = userService.findByEmail(user.getEmail());
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void findByEmail_whenUserNotExists_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findByEmail("nonexistent@example.com");
        });
    }

    @Test
    void existsByEmail_shouldCallRepository() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@example.com"));
        verify(userRepository).existsByEmail("test@example.com");
    }
/*

    @Test
    void convertToDto_shouldConvertUserToUserDto() {
        UserDto userDto = userService.convertToDto(user);
        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getFirstName(), userDto.getFirstName());
        assertEquals(user.getLastName(), userDto.getLastName());
        assertEquals(user.getProfileImageUrl(), userDto.getProfileImageUrl());
        assertEquals(user.getRoles(), userDto.getRoles());
        assertEquals(user.getCreatedAt(), userDto.getCreatedAt());
        // Note: UserDto does not currently include updatedAt, so no assertion for it here
    }
*/
/*

    @Test
    void convertToDto_withNullUser_shouldReturnNull() {
        assertNull(userService.convertToDto(null));
    }
*/

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_whenUserNotExists_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent@example.com");
        });
    }

    @Test
    void getCurrentAuthenticatedUser_whenPrincipalIsUserInstance_shouldReturnUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        User currentUser = userService.getCurrentAuthenticatedUser();
        assertNotNull(currentUser);
        assertEquals(user.getEmail(), currentUser.getEmail());
    }

    @Test
    void getCurrentAuthenticatedUser_whenPrincipalIsUserDetails_shouldFetchAndReturnUser() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(user.getEmail());

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User currentUser = userService.getCurrentAuthenticatedUser();
        assertNotNull(currentUser);
        assertEquals(user.getEmail(), currentUser.getEmail());
    }

    @Test
    void getCurrentAuthenticatedUser_whenPrincipalIsString_shouldFetchAndReturnUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User currentUser = userService.getCurrentAuthenticatedUser();
        assertNotNull(currentUser);
        assertEquals(user.getEmail(), currentUser.getEmail());
    }

    @Test
    void getCurrentAuthenticatedUser_whenNotAuthenticated_shouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(false); // Not authenticated

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getCurrentAuthenticatedUser();
        });
    }

    @Test
    void getCurrentAuthenticatedUser_whenPrincipalIsAnonymous_shouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getCurrentAuthenticatedUser();
        });
    }
}
