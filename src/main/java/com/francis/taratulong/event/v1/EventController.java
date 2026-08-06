package com.francis.taratulong.event.v1;

import com.francis.taratulong.event.Event;
import com.francis.taratulong.event.EventService;
import com.francis.taratulong.event.v1.dto.EventMapper;
import com.francis.taratulong.event.v1.dto.EventRequestDTO;
import com.francis.taratulong.event.v1.dto.EventResponseDTO;
import com.francis.taratulong.user.AppUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Event")
@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO requestDTO,
            @AuthenticationPrincipal AppUser currentOrg) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                eventMapper.toResponseDTO(eventService.saveEvent(currentOrg.getId(), eventMapper.toEntity(requestDTO)))
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
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg,
            @Valid@RequestBody EventRequestDTO requestDTO){
        return ResponseEntity.ok(eventMapper.toResponseDTO(eventService.updateEvent(id, currentOrg.getId(), eventMapper.toEntity(requestDTO))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        eventService.deleteEvent(id, currentOrg.getId());
        return ResponseEntity.noContent().build();
    }


}
