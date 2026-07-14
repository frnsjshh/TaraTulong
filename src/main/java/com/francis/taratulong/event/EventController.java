package com.francis.taratulong.event;

import com.francis.taratulong.event.dto.EventMapper;
import com.francis.taratulong.event.dto.EventRequestDTO;
import com.francis.taratulong.event.dto.EventResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                EventMapper.toResponseDTO(eventService.saveEvent(requestDTO.organizerId(), EventMapper.toEntity(requestDTO)))
        );
    }
    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Event> eventPage = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(eventPage.map(EventMapper::toResponseDTO));
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<Page<EventResponseDTO>> getEventByOrganizer(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Event> eventPage = eventService.getEventByOrganizer(orgId, page, size);
        return ResponseEntity.ok(eventPage.map(EventMapper::toResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(EventMapper.toResponseDTO(eventService.getEvent(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable Long id, @Valid@RequestBody EventRequestDTO requestDTO){
        return ResponseEntity.ok(EventMapper.toResponseDTO(eventService.updateEvent(id, EventMapper.toEntity(requestDTO))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


}
