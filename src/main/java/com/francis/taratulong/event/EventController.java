package com.francis.taratulong.event;

import com.francis.taratulong.event.dto.EventMapper;
import com.francis.taratulong.event.dto.EventRequestDTO;
import com.francis.taratulong.event.dto.EventResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public EventResponseDTO createEvent(@Valid @RequestBody EventRequestDTO requestDTO) {
        return EventMapper.toResponseDTO(eventService.saveEvent(requestDTO.organizerId(), EventMapper.toEntity(requestDTO)));
    }
    @GetMapping
    public Page<EventResponseDTO> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Event> eventPage = eventService.getAllEvents(page, size);
        return eventPage.map(EventMapper::toResponseDTO);
    }

    @GetMapping("/org/{orgId}")
    public Page<EventResponseDTO> getEventByOrganizer(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Event> eventPage = eventService.getEventByOrganizer(orgId, page, size);
        return eventPage.map(EventMapper::toResponseDTO);
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
