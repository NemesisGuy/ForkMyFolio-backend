package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.TokenRefreshException;
import com.forkmyfolio.model.RefreshToken;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.RefreshTokenRepository;
import com.forkmyfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    private final Long refreshTokenDurationMs = 3600000L; // 1 hour for testing
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;
    private User user;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to set the @Value field
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", refreshTokenDurationMs);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void findByToken_shouldReturnToken_whenTokenExists() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(user, tokenValue, Instant.now().plusMillis(refreshTokenDurationMs));
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> foundToken = refreshTokenService.findByToken(tokenValue);

        assertTrue(foundToken.isPresent());
        assertEquals(tokenValue, foundToken.get().getToken());
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    void createRefreshToken_shouldCreateAndSaveNewToken() {
        // Mock the behavior of findByUser to return empty, indicating no existing token for this user
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        // Mock the save operation
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken createdToken = refreshTokenService.createRefreshToken(user);

        assertNotNull(createdToken);
        assertEquals(user, createdToken.getUser());
        assertNotNull(createdToken.getToken());
        assertTrue(createdToken.getExpiryDate().isAfter(Instant.now()));
        // Check if expiry is approximately correct
        assertTrue(createdToken.getExpiryDate().isBefore(Instant.now().plusMillis(refreshTokenDurationMs + 1000)));

        verify(refreshTokenRepository).findByUser(user); // Verify it checked for existing token
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_shouldDeleteOldTokenAndSaveNewToken_whenOldTokenExists() {
        String oldTokenValue = UUID.randomUUID().toString();
        RefreshToken oldRefreshToken = new RefreshToken(user, oldTokenValue, Instant.now().plusMillis(10000)); // Some old expiry

        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(oldRefreshToken));
        doNothing().when(refreshTokenRepository).delete(oldRefreshToken); // Mock deletion of old token
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken newToken = invocation.getArgument(0);
            assertNotEquals(oldTokenValue, newToken.getToken()); // Ensure new token is different
            return newToken;
        });

        RefreshToken createdToken = refreshTokenService.createRefreshToken(user);

        assertNotNull(createdToken);
        assertEquals(user, createdToken.getUser());
        assertNotEquals(oldTokenValue, createdToken.getToken()); // Verify new token is different
        assertTrue(createdToken.getExpiryDate().isAfter(Instant.now()));

        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).delete(oldRefreshToken); // Verify old token was deleted
        verify(refreshTokenRepository).save(any(RefreshToken.class)); // Verify new token was saved
    }


    @Test
    void verifyExpiration_shouldReturnToken_whenNotExpired() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(user, tokenValue, Instant.now().plus(1, ChronoUnit.HOURS));

        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(refreshToken);

        assertNotNull(verifiedToken);
        assertEquals(tokenValue, verifiedToken.getToken());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_shouldThrowExceptionAnddeleteToken_whenExpired() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(user, tokenValue, Instant.now().minus(1, ChronoUnit.HOURS));

        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.verifyExpiration(refreshToken);
        });

        assertEquals(String.format("Failed for [%s]: Refresh token was expired. Please make a new sign-in request", tokenValue), exception.getMessage());
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void deleteByUserId_shouldCallRepositoryDeleteByUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.deleteByUser(user)).thenReturn(1); // Assume 1 token deleted

        int deletedCount = refreshTokenService.deleteByUserId(user.getId());

        assertEquals(1, deletedCount);
        verify(userRepository).findById(user.getId());
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void deleteByToken_shouldCallRepositoryDelete_whenTokenExists() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(user, tokenValue, Instant.now().plusMillis(refreshTokenDurationMs));
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        doNothing().when(refreshTokenRepository).delete(refreshToken);

        refreshTokenService.deleteByToken(tokenValue);

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void deleteByToken_shouldDoNothing_whenTokenDoesNotExist() {
        String tokenValue = UUID.randomUUID().toString();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        refreshTokenService.deleteByToken(tokenValue);

        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }
}
