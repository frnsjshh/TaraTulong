package com.francis.taratulong.user.volunteer;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// ──────────────────────────────────────────────────────────────────────
// Same pattern as RegistrationServiceTest:
// @ExtendWith activates Mockito → @Mock creates fakes → @InjectMocks
// wires them into the real VolunteerService.
// ──────────────────────────────────────────────────────────────────────
@ExtendWith(MockitoExtension.class)
class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private VolunteerService volunteerService;

    // ──────────────────────────────────────────────────────────────────
    // Shared fixture: a reusable Volunteer object rebuilt before each test.
    // ──────────────────────────────────────────────────────────────────
    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        volunteer = new Volunteer();
        volunteer.setId(1L);
        volunteer.setEmail("juan@email.com");
        volunteer.setPassword("rawPassword");
        volunteer.setFirstName("Juan");
        volunteer.setLastName("Dela Cruz");
        volunteer.setTrustScore(50);
        volunteer.setTotalEventsAttended(0);
        volunteer.setTotalApprovedRegistrations(0);
    }

    // ══════════════════════════════════════════════════════════════════
    // saveVolunteer
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("saveVolunteer")
    class SaveVolunteer {

        @Test
        @DisplayName("should save volunteer when email is not yet taken")
        void happyPath() {
            // ARRANGE
            when(volunteerRepository.findByEmail("juan@email.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> {
                Volunteer saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // ACT
            Volunteer result = volunteerService.saveVolunteer(volunteer);

            // ASSERT
            assertNotNull(result);
            assertEquals("encodedPassword", result.getPassword());
            assertEquals(Role.VOLUNTEER, result.getRole());
            verify(volunteerRepository, times(1)).save(any(Volunteer.class));
        }

        // ──────────────────────────────────────────────────────────────
        // Testing that the password gets ENCODED before saving.
        // This is critical — we never store raw passwords.
        // We verify the encoder was called AND the saved entity has
        // the encoded value, not the raw one.
        // ──────────────────────────────────────────────────────────────
        @Test
        @DisplayName("should encode password before saving")
        void shouldEncodePassword() {
            when(volunteerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$hashed");
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(i -> i.getArgument(0));

            Volunteer result = volunteerService.saveVolunteer(volunteer);

            assertEquals("$2a$10$hashed", result.getPassword());
            verify(passwordEncoder).encode("rawPassword");
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when email is already taken")
        void shouldThrowWhenEmailExists() {
            // ARRANGE: repository finds an existing user with the same email
            when(volunteerRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(volunteer));

            // ACT & ASSERT
            UserAlreadyExistsException exception = assertThrows(
                    UserAlreadyExistsException.class,
                    () -> volunteerService.saveVolunteer(volunteer)
            );

            assertEquals("User already exist", exception.getMessage());
            // Prove save was never called
            verify(volunteerRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // getVolunteer
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getVolunteer")
    class GetVolunteer {

        @Test
        @DisplayName("should return volunteer when found")
        void happyPath() {
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            Volunteer result = volunteerService.getVolunteer(1L);

            assertEquals(1L, result.getId());
            assertEquals("Juan", result.getFirstName());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(volunteerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> volunteerService.getVolunteer(999L)
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // updateVolunteer
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateVolunteer")
    class UpdateVolunteer {

        @Test
        @DisplayName("should update firstName and lastName")
        void happyPath() {
            // ARRANGE
            Volunteer updateRequest = new Volunteer();
            updateRequest.setFirstName("Pedro");
            updateRequest.setLastName("Santos");
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            // ACT
            Volunteer result = volunteerService.updateVolunteer(1L, updateRequest);

            // ASSERT
            assertEquals("Pedro", result.getFirstName());
            assertEquals("Santos", result.getLastName());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when volunteer doesn't exist")
        void shouldThrowWhenNotFound() {
            when(volunteerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> volunteerService.updateVolunteer(999L, new Volunteer())
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Stat Update Methods
    //
    // These methods apply DELTA values (not absolute values).
    // We test that they correctly ADD the delta to the current value.
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateEventsAttended")
    class UpdateEventsAttended {

        @Test
        @DisplayName("should increment events attended by the given amount")
        void shouldIncrement() {
            volunteer.setTotalEventsAttended(5);
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            volunteerService.updateEventsAttended(1L, 1);

            assertEquals(6, volunteer.getTotalEventsAttended());
        }

        @Test
        @DisplayName("should decrement events attended when given a negative amount")
        void shouldDecrement() {
            volunteer.setTotalEventsAttended(5);
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            volunteerService.updateEventsAttended(1L, -1);

            assertEquals(4, volunteer.getTotalEventsAttended());
        }
    }

    @Nested
    @DisplayName("updateTotalRegistrations")
    class UpdateTotalRegistrations {

        @Test
        @DisplayName("should increment total registrations")
        void shouldIncrement() {
            volunteer.setTotalApprovedRegistrations(10);
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            volunteerService.updateTotalRegistrations(1L, 1);

            assertEquals(11, volunteer.getTotalApprovedRegistrations());
        }
    }

    @Nested
    @DisplayName("updateTrustScore")
    class UpdateTrustScore {

        @Test
        @DisplayName("should add positive score to trust score")
        void shouldAddPositive() {
            volunteer.setTrustScore(50);
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            volunteerService.updateTrustScore(1L, 10);

            assertEquals(60, volunteer.getTrustScore());
        }

        @Test
        @DisplayName("should subtract negative score from trust score")
        void shouldSubtractNegative() {
            volunteer.setTrustScore(50);
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            volunteerService.updateTrustScore(1L, -25);

            assertEquals(25, volunteer.getTrustScore());
        }
    }
}
