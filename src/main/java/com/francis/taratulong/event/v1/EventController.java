package com.francis.taratulong.event.v1;

import com.francis.taratulong.event.Event;
import com.francis.taratulong.event.EventService;
import com.francis.taratulong.event.v1.dto.EventMapper;
import com.francis.taratulong.event.v1.dto.EventRequestDTO;
import com.francis.taratulong.event.v1.dto.EventResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                eventMapper.toResponseDTO(eventService.saveEvent(requestDTO.organizerId(), eventMapper.toEntity(requestDTO)))
        );
    }
    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Event> eventPage = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(eventPage.map(eventMapper::toResponseDTO));
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<Page<EventResponseDTO>> getEventByOrganizer(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Event> eventPage = eventService.getEventByOrganizer(orgId, page, size);
        return ResponseEntity.ok(eventPage.map(eventMapper::toResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventMapper.toResponseDTO(eventService.getEvent(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable Long id, @Valid@RequestBody EventRequestDTO requestDTO){
        return ResponseEntity.ok(eventMapper.toResponseDTO(eventService.updateEvent(id, eventMapper.toEntity(requestDTO))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


}
