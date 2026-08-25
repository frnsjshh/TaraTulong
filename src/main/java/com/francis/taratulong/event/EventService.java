package com.francis.taratulong.event;

import com.francis.taratulong.exception.EventNotFoundException;
import com.francis.taratulong.exception.InvalidDateRangeException;
import com.francis.taratulong.exception.UnauthorizedAccessException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.organization.OrgRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final OrgRepository orgRepository;


    public Event saveEvent(Long orgId, Event event) {
        log.debug("Attempting to save event for orgId={}", orgId);
        checkDateRangeIfValid(event);
        Org org = orgRepository.findById(orgId).orElseThrow(()->new UserNotFoundException("Cannot create event. Organization not found."));
        event.setOrganizer(org);
        Event saved = eventRepository.save(event);
        log.info("Event created: id={}, title='{}', orgId={}", saved.getId(), saved.getTitle(), orgId);
        return saved;
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found"));
    }

    public Event getEvent(Long id, String errorMsg) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(errorMsg));
    }

    public Event updateEvent(Long id, Long orgId, Event event){
        log.debug("Attempting to update eventId={} by orgId={}", id, orgId);
        checkDateRangeIfValid(event);
        Event eventDB = getEvent(id,"Cannot update event. Event not found.");
        checkOwnership(id, orgId, "Cannot update event. Unauthorized");
        eventDB.setTitle(event.getTitle());
        eventDB.setDescription(event.getDescription());
        eventDB.setStartDateTime(event.getStartDateTime());
        eventDB.setEndDateTime(event.getEndDateTime());
        eventDB.setCutOffTime(event.getCutOffTime());
        eventDB.setLocation(event.getLocation());
        eventDB.setSlotsAvailable(event.getSlotsAvailable());
        log.info("Event updated: id={}, title='{}'", id, eventDB.getTitle());
        return eventDB;
    }

    public void deleteEvent(Long id, Long orgId) {
        log.debug("Attempting to delete eventId={} by orgId={}", id, orgId);
        Event event = getEvent(id, "Cannot delete event. Event not found.");
        checkOwnership(id, orgId, "Cannot delete event. Unauthorized");
        event.setDeleted(true);
        log.info("Event soft-deleted: id={}", id);
    }

    public Page<Event> getEventByOrganizer(Long orgId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").descending());
        return eventRepository.findByOrganizerId(orgId, pageable);
    }

    public Page<Event> getAllEvents(int page, int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").descending());
            return eventRepository.findAll(pageable);
    }

    public Long getOrganizer(Long eventId) {
        return getEvent(eventId).getOrganizer().getId();
    }

    public void checkOwnership(Long eventId, Long orgId, String errorMsg) {
        if(!getOrganizer(eventId).equals(orgId)){
            log.warn("Ownership check failed: eventId={}, requestingOrgId={}", eventId, orgId);
            throw new UnauthorizedAccessException(errorMsg);
        }
    }

    public void checkDateRangeIfValid(Event event) {
        if(event.getStartDateTime().isAfter(event.getEndDateTime())) {
            log.warn("Invalid date range: start={} is after end={}", event.getStartDateTime(), event.getEndDateTime());
            throw new InvalidDateRangeException("End date must be after the start date");
        }
    }
}
