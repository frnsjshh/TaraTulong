package com.francis.taratulong.user;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail("user@email.com");
        appUser.setPassword("encodedCurrentPassword");
        appUser.setRole(Role.VOLUNTEER);
    }

    // ══════════════════════════════════════════════════════════════════
    // updateEmail
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateEmail")
    class UpdateEmail {

        @Test
        @DisplayName("should update email when new email is not yet taken")
        void happyPath() {
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(appUserRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());

            appUserService.updateEmail(1L, "new@email.com");

            assertEquals("new@email.com", appUser.getEmail());
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when email is already in use")
        void shouldThrowWhenEmailTaken() {
            AppUser otherUser = new AppUser();
            otherUser.setId(2L);
            otherUser.setEmail("taken@email.com");

            when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(appUserRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(otherUser));

            UserAlreadyExistsException exception = assertThrows(
                    UserAlreadyExistsException.class,
                    () -> appUserService.updateEmail(1L, "taken@email.com")
            );

            assertEquals("Cannot update email. Email already in use", exception.getMessage());
            // Email should NOT have changed
            assertEquals("user@email.com", appUser.getEmail());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user doesn't exist")
        void shouldThrowWhenUserNotFound() {
            when(appUserRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> appUserService.updateEmail(999L, "new@email.com")
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // updatePassword
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("should update password when current password matches")
        void happyPath() {
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
            // passwordEncoder.matches() checks raw against encoded
            when(passwordEncoder.matches("rawCurrentPassword", "encodedCurrentPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

            appUserService.updatePassword(1L, "rawCurrentPassword", "newPassword");

            assertEquals("encodedNewPassword", appUser.getPassword());
            verify(passwordEncoder).encode("newPassword");
        }


        @Test
        @DisplayName("should throw UserNotFoundException when user doesn't exist")
        void shouldThrowWhenUserNotFound() {
            when(appUserRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> appUserService.updatePassword(999L, "current", "new")
            );
        }
        @Test
        @DisplayName("should throw BadCredentialsException when current password doesn't match")
        void shouldThrowWhenBadCredentials() {
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
            when(passwordEncoder.matches("wrongPassword", "encodedCurrentPassword")).thenReturn(false);

            assertThrows(
                    BadCredentialsException.class,
                    () -> appUserService.updatePassword(1L, "wrongPassword", "new")
            );
            // Password should remain unchanged
            assertEquals("encodedCurrentPassword", appUser.getPassword());
            // encode() should never be called since the match failed
            verify(passwordEncoder, never()).encode(anyString());
        }
    }
}
