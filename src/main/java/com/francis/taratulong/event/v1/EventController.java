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

import java.util.List;
import java.util.UUID;

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
                eventMapper.toResponseDTO(
                        eventService.saveEvent(
                                currentOrg.getId(),
                                eventMapper.toEntity(requestDTO),
                                requestDTO.locationId(),
                                requestDTO.categories()
                        )
                )
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

    /**
     * Search/filter events by location hierarchy and/or categories.
     * All query params are optional — omit to skip that filter.
     *
     * @param locationId   filter by exact city/municipality UUID
     * @param provinceId   filter by province UUID (includes all cities under it)
     * @param regionId     filter by region UUID (includes all provinces and cities under it)
     * @param categoryNames filter by category names (OR logic), comma-separated
     * @param page         page number (0-indexed)
     * @param size         page size
     */
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponseDTO>> searchEvents(
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID provinceId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) List<String> categoryNames,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Event> eventPage = eventService.searchEvents(
                locationId, provinceId, regionId, categoryNames, page, size
        );
        return ResponseEntity.ok(eventPage.map(eventMapper::toResponseDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg,
            @Valid@RequestBody EventRequestDTO requestDTO){
        return ResponseEntity.ok(
                eventMapper.toResponseDTO(
                        eventService.updateEvent(
                                id,
                                currentOrg.getId(),
                                eventMapper.toEntity(requestDTO),
                                requestDTO.locationId(),
                                requestDTO.categories()
                        )
                )
        );
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
