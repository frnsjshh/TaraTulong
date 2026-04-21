package com.francis.taratulong.event;

import com.francis.taratulong.event.dto.EventMapper;
import com.francis.taratulong.event.dto.EventRequestDTO;
import com.francis.taratulong.event.dto.EventResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public EventResponseDTO createEvent(@Valid@RequestBody EventRequestDTO requestDTO) {
        return EventMapper.toResponseDTO(eventService.saveEvent(requestDTO.organizerId(), EventMapper.toEntity(requestDTO)));
    }
    @GetMapping
    public List<EventResponseDTO> getAllEvents() {
        return eventService.getAllEvents().stream().map(EventMapper::toResponseDTO).toList();
    }

    @GetMapping("/org/{orgId}")
    public List<EventResponseDTO> getEventByOrganizer(@PathVariable Long orgId){
        return eventService.getEventByOrganizer(orgId).stream().map(EventMapper::toResponseDTO).toList();
    }

    @GetMapping("/{id}")
    public EventResponseDTO getEvent(@PathVariable Long id) {
        return EventMapper.toResponseDTO(eventService.getEvent(id));
    }

    @PutMapping("/{id}")
    public EventResponseDTO updateEvent(@PathVariable Long id, @Valid@RequestBody EventRequestDTO requestDTO){
        return EventMapper.toResponseDTO(eventService.updateEvent(id, EventMapper.toEntity(requestDTO)));
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }


}
