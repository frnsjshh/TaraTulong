package com.francis.taratulong.event;

import com.francis.taratulong.exception.EventNotFoundException;
import com.francis.taratulong.exception.InvalidDateRangeException;
import com.francis.taratulong.exception.UnauthorizedAccessException;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.organization.OrgRepository;
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

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private OrgRepository orgRepository;
    @Mock
    private com.francis.taratulong.location.LocationService locationService;
    @Mock
    private com.francis.taratulong.category.CategoryService categoryService;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private Org org;

    @BeforeEach
    void setUp() {
        org = new Org();
        org.setId(100L);

        event = new Event();
        event.setId(10L);
        event.setOrganizer(org);
        event.setTitle("Test Event");
        event.setDescription("This is a test event");
        event.setStartDateTime(LocalDateTime.now().plusDays(1));
        event.setEndDateTime(LocalDateTime.now().plusDays(2));
    }

    @Nested
    @DisplayName("saveEvent")
    class SaveEvent {

        @Test
        @DisplayName("should save an event when when start date before the end date ")
        void happyPath() {
            // arrange
            event.setStartDateTime(LocalDateTime.now().plusDays(1));
            event.setEndDateTime(LocalDateTime.now().plusDays(2));
            when(orgRepository.findById(100L)).thenReturn(Optional.of(org));
            when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
                Event saved = invocation.getArgument(0);
                saved.setId(1000L);
                return saved;
            });
            // Act
            java.util.UUID locationId = java.util.UUID.randomUUID();
            java.util.Set<String> categories = java.util.Set.of("tag1");
            
            com.francis.taratulong.location.Location location = new com.francis.taratulong.location.Location();
            location.setId(locationId);
            when(locationService.getLocationById(locationId, "Cannot create event. Location not found.")).thenReturn(location);
            when(categoryService.resolveCategories(categories)).thenReturn(java.util.Set.of(new com.francis.taratulong.category.Category()));

            Event result = eventService.saveEvent(100L, event, locationId, categories);

            // Assert
            assertNotNull(result);
            assertEquals(org, result.getOrganizer());
            assertEquals(1000L, result.getId());

            verify(eventRepository, times(1)).save(any(Event.class));
        }

    }

    @Nested
    @DisplayName("getEvent")
    class GetEvent {
        @Test
        @DisplayName("event exists, should return event")
        void happyPath() {
            // arrange
            when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
            // act
            Event result = eventService.getEvent(10L);
            // assert
            assertEquals(event, result);
            verify(eventRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("should throw EventNotFoundException when no event of ID is found")
        void shouldThrowWhenNotFound() {
            when(eventRepository.findById(10L)).thenReturn(Optional.empty());

            EventNotFoundException exception = assertThrows(
                    EventNotFoundException.class,
                    () -> eventService.getEvent(10L));
            assertEquals("Event not found", exception.getMessage());

            verify(eventRepository, times(1)).findById(10L);
        }
    }

    @Nested
    @DisplayName("updateEvent")
    class UpdateEvent {
        @Test
        @DisplayName("should update all fields of an existing registration ")
        void happyPath() {
            // arrange
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fiveDaysLater = now.plusDays(5);
            when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
            Event updatedEvent = new Event();
            updatedEvent.setTitle("Updated Title");
            updatedEvent.setDescription("Updated Description");
            updatedEvent.setStartDateTime(now);
            updatedEvent.setEndDateTime(fiveDaysLater);

            // act
            java.util.UUID locationId = java.util.UUID.randomUUID();
            java.util.Set<String> categories = java.util.Set.of("tag1");

            com.francis.taratulong.location.Location location = new com.francis.taratulong.location.Location();
            location.setId(locationId);
            when(locationService.getLocationById(locationId, "Cannot update event. Location not found.")).thenReturn(location);
            when(categoryService.resolveCategories(categories)).thenReturn(java.util.Set.of(new com.francis.taratulong.category.Category()));

            Event result = eventService.updateEvent(10L, 100L, updatedEvent, locationId, categories);

            // assert
            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated Description", result.getDescription());
            assertEquals(now, result.getStartDateTime());
            assertEquals(fiveDaysLater, result.getEndDateTime());
        }

        @Test
        @DisplayName("should throw UnauthorizedAccessException when updating Event when currentOrg is not equal to the Event's org")
        void shouldThrowWhenUnauthorized() {
            // arrange
            when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

            // act & assert
            assertThrows(
                    UnauthorizedAccessException.class,
                    () -> eventService.updateEvent(10L, 101L, event, null, null));
        }

    }

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEvent {

        @Test
        @DisplayName("should Set Event Deleted to true")
        void happyPath() {
            // arrange
            when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

            // act
            eventService.deleteEvent(10L, 100L);

            // assert
            assertTrue(event.isDeleted());
        }

    }

    @Test
    @DisplayName("should throw InvalidDateRangeException when start date is after the end date ")
    void shouldThrowWhenStartAfterEnd() {
        // ARRANGE
        event.setStartDateTime(LocalDateTime.now().plusDays(2));
        event.setEndDateTime(LocalDateTime.now().plusDays(1));

        // ACT & ASSERT
        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> eventService.saveEvent(100L, event, null, null));

        assertEquals("End date must be after the start date", exception.getMessage());

        // prove that we never called save
        verify(eventRepository, never()).save(any());
    }

}