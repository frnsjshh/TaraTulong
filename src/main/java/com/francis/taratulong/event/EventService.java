package com.francis.taratulong.event;

import com.francis.taratulong.exception.EventNotFoundException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.organization.OrgRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final OrgRepository orgRepository;


    public Event saveEvent(Long orgId, Event event) {
        Org org = orgRepository.findById(orgId).orElseThrow(()->new UserNotFoundException("Cannot create event. Organization not found."));
        event.setOrganizer(org);
        return eventRepository.save(event);
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found"));
    }

    public Event updateEvent(Long id, Long orgId, Event event){
        Event eventDB = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Cannot update event. Event not found."));
        if(!eventDB.getOrganizer().getId().equals(orgId)) throw new UserNotFoundException("Cannot update event. Unauthorized");
        eventDB.setTitle(event.getTitle());
        eventDB.setDescription(event.getDescription());
        eventDB.setStartDateTime(event.getStartDateTime());
        eventDB.setEndDateTime(event.getEndDateTime());
        eventDB.setCutOffTime(event.getCutOffTime());
        eventDB.setLocation(event.getLocation());
        eventDB.setSlotsAvailable(event.getSlotsAvailable());
        return eventDB;
    }

    public void deleteEvent(Long id, Long orgId) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Cannot delete event. Event not found."));
        if(!event.getOrganizer().getId().equals(orgId)) throw new UserNotFoundException("Cannot delete event. Unauthorized");
        event.setDeleted(true);
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
}
