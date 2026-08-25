package com.francis.taratulong.registration;

import com.francis.taratulong.Status;
import com.francis.taratulong.event.Event;
import com.francis.taratulong.event.EventService;
import com.francis.taratulong.exception.*;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.volunteer.Volunteer;
import com.francis.taratulong.user.volunteer.VolunteerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ──────────────────────────────────────────────────────────────────────
// CONCEPT 1: @ExtendWith(MockitoExtension.class)
//
// This tells JUnit 5 to activate the Mockito extension.
// The extension scans this class for @Mock and @InjectMocks annotations
// and initializes them BEFORE each test runs.
// Without this, your @Mock fields would be null.
// ──────────────────────────────────────────────────────────────────────
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    // ──────────────────────────────────────────────────────────────────
    // CONCEPT 2: @Mock
    //
    // Creates a "fake" (mock) object for each dependency.
    // A mock records calls made to it and returns default values
    // (null for objects, 0 for numbers, false for booleans)
    // unless you tell it otherwise with when(...).thenReturn(...).
    //
    // WHY: We don't want the REAL repository hitting a REAL database.
    //      We only want to test the logic inside RegistrationService.
    // ──────────────────────────────────────────────────────────────────
    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private VolunteerService volunteerService;

    @Mock
    private EventService eventService;

    // ──────────────────────────────────────────────────────────────────
    // CONCEPT 3: @InjectMocks
    //
    // Creates a REAL instance of RegistrationService and injects all
    // the @Mock objects above into its constructor.
    //
    // Since RegistrationService uses @RequiredArgsConstructor (Lombok),
    // Mockito calls new RegistrationService(mockRepo, mockVolSvc, mockEvtSvc).
    // ──────────────────────────────────────────────────────────────────
    @InjectMocks
    private RegistrationService registrationService;

    // ──────────────────────────────────────────────────────────────────
    // CONCEPT 4: Shared Test Fixtures
    //
    // These are reusable objects that many tests need.
    // @BeforeEach rebuilds them fresh before EVERY test so that
    // one test's mutations don't leak into another.
    // ──────────────────────────────────────────────────────────────────
    private Volunteer volunteer;
    private Event event;
    private Registration registration;

    @BeforeEach
    void setUp() {
        Org org = new Org();
        org.setId(100L);

        volunteer = new Volunteer();
        volunteer.setId(1L);
        volunteer.setFirstName("Juan");
        volunteer.setLastName("Dela Cruz");

        event = new Event();
        event.setId(10L);
        event.setOrganizer(org);
        event.setSlotsAvailable(5);
        event.setCutOffTime(LocalDateTime.now().plusDays(3));       // still open
        event.setStartDateTime(LocalDateTime.now().plusDays(7));    // starts in a week

        registration = new Registration();
        registration.setId(1000L);
        registration.setVolunteer(volunteer);
        registration.setEvent(event);
        registration.setRegistrationStatus(Status.PENDING);
        registration.setAttendanceStatus(AttendanceStatus.PENDING);
    }

    // ══════════════════════════════════════════════════════════════════
    // CONCEPT 5: @Nested classes
    //
    // Groups related tests together. Think of each @Nested class as a
    // chapter: "Tests for saving", "Tests for approving", etc.
    // This makes the test report tree-structured and easy to scan.
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveRegistration")
    class SaveRegistration {

        // ──────────────────────────────────────────────────────────────
        // CONCEPT 6: The HAPPY PATH
        //
        // Always start testing with the scenario where everything works.
        // This is the "golden path" — valid input, no errors.
        //
        // PATTERN:
        //   1. ARRANGE – set up mocks with when(...).thenReturn(...)
        //   2. ACT     – call the method under test
        //   3. ASSERT  – verify the result and/or interactions
        // ──────────────────────────────────────────────────────────────
        @Test
        @DisplayName("should save registration when event is open and volunteer is not yet registered")
        void happyPath() {
            // ARRANGE: tell each mock what to return when called
            when(eventService.getEvent(10L)).thenReturn(event);
            when(registrationRepository.existsByVolunteerIdAndEventId(1L, 10L)).thenReturn(false);
            when(volunteerService.getVolunteer(1L)).thenReturn(volunteer);
            when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
                // Return the same object that was passed to save()
                Registration saved = invocation.getArgument(0);
                saved.setId(1000L);
                return saved;
            });

            // ACT
            Registration result = registrationService.saveRegistration(1L, 10L);

            // ASSERT
            assertNotNull(result);
            assertEquals(volunteer, result.getVolunteer());
            assertEquals(event, result.getEvent());

            // ──────────────────────────────────────────────────────────
            // CONCEPT 7: verify()
            //
            // Checks that a mock method was called the expected number
            // of times. This proves your service actually delegated to
            // the repository.
            // ──────────────────────────────────────────────────────────
            verify(registrationRepository, times(1)).save(any(Registration.class));
        }

        // ──────────────────────────────────────────────────────────────
        // CONCEPT 8: Testing EXCEPTION / EDGE CASES
        //
        // After the happy path, test every branch that throws.
        // Use assertThrows() to verify BOTH:
        //   (a) the correct exception TYPE is thrown
        //   (b) the error message is what you expect
        //
        // Also use verify(..., never()) to prove that downstream calls
        // (like save) were NOT made when validation failed.
        // ──────────────────────────────────────────────────────────────

        @Test
        @DisplayName("should throw EventRegistrationClosed when cutoff has passed")
        void shouldThrowWhenCutoffPassed() {
            // ARRANGE: set cutoff to the past
            event.setCutOffTime(LocalDateTime.now().minusDays(1));
            when(eventService.getEvent(10L)).thenReturn(event);

            // ACT & ASSERT
            EventRegistrationClosed exception = assertThrows(
                    EventRegistrationClosed.class,
                    () -> registrationService.saveRegistration(1L, 10L)
            );

            assertEquals("Registration for this event has closed.", exception.getMessage());

            // Prove that we never even tried to save
            verify(registrationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw EventRegistrationClosed when no slots available")
        void shouldThrowWhenNoSlots() {
            event.setSlotsAvailable(0);
            when(eventService.getEvent(10L)).thenReturn(event);

            assertThrows(
                    EventRegistrationClosed.class,
                    () -> registrationService.saveRegistration(1L, 10L)
            );
            verify(registrationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw VolunteerAlreadyRegisteredException when duplicate registration")
        void shouldThrowWhenAlreadyRegistered() {
            when(eventService.getEvent(10L)).thenReturn(event);
            when(registrationRepository.existsByVolunteerIdAndEventId(1L, 10L)).thenReturn(true);

            assertThrows(
                    VolunteerAlreadyRegisteredException.class,
                    () -> registrationService.saveRegistration(1L, 10L)
            );
            verify(registrationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getRegistration")
    class GetRegistration {

        @Test
        @DisplayName("should return registration when found")
        void happyPath() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            Registration result = registrationService.getRegistration(1000L);

            assertEquals(1000L, result.getId());
        }

        @Test
        @DisplayName("should throw RegistrationNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(registrationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    RegistrationNotFoundException.class,
                    () -> registrationService.getRegistration(999L)
            );
        }
    }

    @Nested
    @DisplayName("setApproved")
    class SetApproved {

        @Test
        @DisplayName("should approve a pending registration and decrement slots")
        void happyPath() {
            // Registration starts as PENDING (set in @BeforeEach)
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            registrationService.setApproved(1000L, 100L);

            assertEquals(Status.APPROVED, registration.getRegistrationStatus());
            assertEquals(4, event.getSlotsAvailable()); // was 5, now 4
        }

        @Test
        @DisplayName("should throw RegistrationConflictException when already approved")
        void shouldThrowWhenAlreadyApproved() {
            registration.setRegistrationStatus(Status.APPROVED);
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.setApproved(1000L, 100L)
            );
        }

        @Test
        @DisplayName("should throw EventRegistrationClosed when no slots left")
        void shouldThrowWhenNoSlotsForApproval() {
            event.setSlotsAvailable(0);
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    EventRegistrationClosed.class,
                    () -> registrationService.setApproved(1000L, 100L)
            );
        }

        // ──────────────────────────────────────────────────────────────
        // CONCEPT 9: Testing AUTHORIZATION
        //
        // Your service has verifyOrgOwnershipToRegistration() which
        // throws UnauthorizedAccessException if the org doesn't own the
        // event. We test that the guard works by passing a WRONG orgId.
        // ──────────────────────────────────────────────────────────────
        @Test
        @DisplayName("should throw UnauthorizedAccessException when org doesn't own the event")
        void shouldThrowWhenUnauthorized() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    UnauthorizedAccessException.class,
                    () -> registrationService.setApproved(1000L, 999L) // wrong org ID
            );
        }
    }

    @Nested
    @DisplayName("setRejected")
    class SetRejected {

        @Test
        @DisplayName("should reject a pending registration")
        void shouldRejectPending() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            registrationService.setRejected(1000L, 100L);

            assertEquals(Status.REJECTED, registration.getRegistrationStatus());
        }

        @Test
        @DisplayName("should reject an approved registration and restore slot")
        void shouldRejectApprovedAndRestoreSlot() {
            registration.setRegistrationStatus(Status.APPROVED);
            int slotsBefore = event.getSlotsAvailable();
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            registrationService.setRejected(1000L, 100L);

            assertEquals(Status.REJECTED, registration.getRegistrationStatus());
            assertEquals(slotsBefore + 1, event.getSlotsAvailable());
        }

        @Test
        @DisplayName("should throw RegistrationConflictException when already rejected")
        void shouldThrowWhenAlreadyRejected() {
            registration.setRegistrationStatus(Status.REJECTED);
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.setRejected(1000L, 100L)
            );
        }
    }

    @Nested
    @DisplayName("deleteRegistration")
    class DeleteRegistration {

        @Test
        @DisplayName("should delete a pending registration")
        void shouldDeletePending() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            registrationService.deleteRegistration(1000L, 1L);

            verify(registrationRepository).deleteById(1000L);
        }

        @Test
        @DisplayName("should delete an approved registration and restore slot")
        void shouldDeleteApprovedAndRestoreSlot() {
            registration.setRegistrationStatus(Status.APPROVED);
            int slotsBefore = event.getSlotsAvailable();
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            registrationService.deleteRegistration(1000L, 1L);

            assertEquals(slotsBefore + 1, event.getSlotsAvailable());
            verify(registrationRepository).deleteById(1000L);
        }

        @Test
        @DisplayName("should throw UnauthorizedAccessException when volunteer doesn't own the registration")
        void shouldThrowWhenWrongVolunteer() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    UnauthorizedAccessException.class,
                    () -> registrationService.deleteRegistration(1000L, 999L) // wrong volunteer
            );
            verify(registrationRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw RegistrationNotFoundException when registration doesn't exist")
        void shouldThrowWhenNotFound() {
            when(registrationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    RegistrationNotFoundException.class,
                    () -> registrationService.deleteRegistration(999L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("setRatingAndFeedback")
    class SetRatingAndFeedback {

        @Test
        @DisplayName("should set rating and feedback on an approved registration")
        void happyPath() {
            registration.setRegistrationStatus(Status.APPROVED);
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            Registration result = registrationService.setRatingAndFeedback(
                    1000L, 5, "Great volunteer!", 100L
            );

            assertEquals(5, result.getRating());
            assertEquals("Great volunteer!", result.getFeedback());
        }

        @Test
        @DisplayName("should default feedback to 'No feedback' when blank")
        void shouldDefaultFeedbackWhenBlank() {
            registration.setRegistrationStatus(Status.APPROVED);
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            Registration result = registrationService.setRatingAndFeedback(
                    1000L, 3, "   ", 100L
            );

            assertEquals("No feedback", result.getFeedback());
        }

        @Test
        @DisplayName("should default feedback to 'No feedback' when null")
        void shouldDefaultFeedbackWhenNull() {
            registration.setRegistrationStatus(Status.APPROVED);
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            Registration result = registrationService.setRatingAndFeedback(
                    1000L, 3, null, 100L
            );

            assertEquals("No feedback", result.getFeedback());
        }

        @Test
        @DisplayName("should throw RegistrationConflictException when rating is below 1")
        void shouldThrowWhenRatingTooLow() {
            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.setRatingAndFeedback(1000L, 0, "test", 100L)
            );
        }

        @Test
        @DisplayName("should throw RegistrationConflictException when rating is above 5")
        void shouldThrowWhenRatingTooHigh() {
            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.setRatingAndFeedback(1000L, 6, "test", 100L)
            );
        }

        @Test
        @DisplayName("should throw when registration is not approved")
        void shouldThrowWhenNotApproved() {
            // registration is PENDING by default
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.setRatingAndFeedback(1000L, 4, "test", 100L)
            );
        }
    }

    @Nested
    @DisplayName("cancelRegistration")
    class CancelRegistration {

        // ──────────────────────────────────────────────────────────────
        // CONCEPT 10: Testing TIME-DEPENDENT LOGIC
        //
        // cancelRegistration() returns CANCELLED_EARLY if now + 48h is
        // before the event start, and CANCELLED_LATE otherwise.
        // We control time by setting event.setStartDateTime(...).
        // ──────────────────────────────────────────────────────────────

        @Test
        @DisplayName("should return CANCELLED_EARLY when cancelled > 48h before event start")
        void shouldReturnCancelledEarly() {
            registration.setRegistrationStatus(Status.APPROVED);
            registration.setAttendanceStatus(AttendanceStatus.PENDING);
            event.setStartDateTime(LocalDateTime.now().plusDays(7)); // plenty of time

            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            AttendanceStatus result = registrationService.cancelRegistration(1000L, 1L);

            assertEquals(AttendanceStatus.CANCELLED_EARLY, result);
        }

        @Test
        @DisplayName("should return CANCELLED_LATE when cancelled < 48h before event start")
        void shouldReturnCancelledLate() {
            registration.setRegistrationStatus(Status.APPROVED);
            registration.setAttendanceStatus(AttendanceStatus.PENDING);
            event.setStartDateTime(LocalDateTime.now().plusHours(24)); // less than 48h

            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            AttendanceStatus result = registrationService.cancelRegistration(1000L, 1L);

            assertEquals(AttendanceStatus.CANCELLED_LATE, result);
        }

        @Test
        @DisplayName("should throw UnauthorizedAccessException when volunteer doesn't own registration")
        void shouldThrowWhenWrongVolunteer() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    UnauthorizedAccessException.class,
                    () -> registrationService.cancelRegistration(1000L, 999L)
            );
        }

        @Test
        @DisplayName("should throw RegistrationConflictException when registration is not approved")
        void shouldThrowWhenNotApproved() {
            // PENDING status by default
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.cancelRegistration(1000L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("updateAttendanceStatus")
    class UpdateAttendanceStatus {

        @Test
        @DisplayName("should update attendance from PENDING to PRESENT")
        void shouldUpdateToPresentFromPending() {
            registration.setRegistrationStatus(Status.APPROVED);
            registration.setAttendanceStatus(AttendanceStatus.PENDING);

            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));
            when(eventService.getOrganizer(1000L)).thenReturn(100L);

            registrationService.updateAttendanceStatus(1000L, 100L, AttendanceStatus.PRESENT);

            assertEquals(AttendanceStatus.PRESENT, registration.getAttendanceStatus());

            // Verify trust score was updated (PRESENT.points - PENDING.points = 10 - 0 = 10)
            verify(volunteerService).updateTrustScore(1L, 10);
            // Verify events attended was incremented (went from non-present to present)
            verify(volunteerService).updateEventsAttended(1L, 1);
            // Verify total registrations was incremented (was PENDING)
            verify(volunteerService).updateTotalRegistrations(1L, 1);
        }

        @Test
        @DisplayName("should do nothing when new status equals current status")
        void shouldNoopWhenSameStatus() {
            registration.setRegistrationStatus(Status.APPROVED);
            registration.setAttendanceStatus(AttendanceStatus.PRESENT);

            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));
            when(eventService.getOrganizer(1000L)).thenReturn(100L);

            registrationService.updateAttendanceStatus(1000L, 100L, AttendanceStatus.PRESENT);

            // Should NOT call any update methods since status didn't change
            verify(volunteerService, never()).updateTrustScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("should throw RegistrationConflictException when registration is not approved")
        void shouldThrowWhenNotApproved() {
            // PENDING registration status by default
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));
            when(eventService.getOrganizer(1000L)).thenReturn(100L);

            assertThrows(
                    RegistrationConflictException.class,
                    () -> registrationService.updateAttendanceStatus(1000L, 100L, AttendanceStatus.PRESENT)
            );
        }

        @Test
        @DisplayName("should throw UnauthorizedAccessException when org doesn't own event")
        void shouldThrowWhenWrongOrg() {
            when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));
            when(eventService.getOrganizer(1000L)).thenReturn(200L); // different org owns it

            assertThrows(
                    UnauthorizedAccessException.class,
                    () -> registrationService.updateAttendanceStatus(1000L, 100L, AttendanceStatus.PRESENT)
            );
        }
    }

    @Nested
    @DisplayName("pointShiftAndRegistrationCounter")
    class PointShiftAndRegistrationCounter {

        // ──────────────────────────────────────────────────────────────
        // CONCEPT 11: Testing INTERNAL ALGORITHMS
        //
        // pointShiftAndRegistrationCounter is public, so we can test it
        // directly. We verify the exact calls made to the volunteer
        // service based on the state transition.
        // ──────────────────────────────────────────────────────────────

        @Test
        @DisplayName("PENDING → PRESENT: should add 10 trust, increment registrations & events attended")
        void pendingToPresent() {
            registration.setAttendanceStatus(AttendanceStatus.PENDING);

            registrationService.pointShiftAndRegistrationCounter(
                    AttendanceStatus.PRESENT, AttendanceStatus.PENDING, registration
            );

            // Trust: PRESENT(10) - PENDING(0) = +10
            verify(volunteerService).updateTrustScore(1L, 10);
            // Was PENDING → increment total registrations
            verify(volunteerService).updateTotalRegistrations(1L, 1);
            // Was not present, now present → increment events attended
            verify(volunteerService).updateEventsAttended(1L, 1);
            verify(registrationRepository).save(registration);
        }

        @Test
        @DisplayName("PRESENT → NO_SHOW: should subtract 35 trust and decrement events attended")
        void presentToNoShow() {
            registration.setAttendanceStatus(AttendanceStatus.PRESENT);

            registrationService.pointShiftAndRegistrationCounter(
                    AttendanceStatus.NO_SHOW, AttendanceStatus.PRESENT, registration
            );

            // Trust: NO_SHOW(-25) - PRESENT(10) = -35
            verify(volunteerService).updateTrustScore(1L, -35);
            // Was present, now not present → decrement events attended
            verify(volunteerService).updateEventsAttended(1L, -1);
            // Was NOT pending → don't touch total registrations
            verify(volunteerService, never()).updateTotalRegistrations(anyLong(), anyInt());
        }

        @Test
        @DisplayName("NO_SHOW → CANCELLED_LATE: should adjust trust but not touch events attended")
        void noShowToCancelledLate() {
            registrationService.pointShiftAndRegistrationCounter(
                    AttendanceStatus.CANCELLED_LATE, AttendanceStatus.NO_SHOW, registration
            );

            // Trust: CANCELLED_LATE(-10) - NO_SHOW(-25) = +15
            verify(volunteerService).updateTrustScore(1L, 15);
            // Neither was present nor is present → no events attended change
            verify(volunteerService, never()).updateEventsAttended(anyLong(), anyInt());
        }
    }
}