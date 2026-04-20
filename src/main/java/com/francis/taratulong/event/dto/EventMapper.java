package com.francis.taratulong.event.dto;

import com.francis.taratulong.event.Event;

public final class EventMapper {
    public static EventResponseDTO toResponseDTO(Event event) {
        return new EventResponseDTO(
                event.getOrganizer().getName(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getCutOffTime(),
                event.getLocation(),
                event.getSlotsAvailable()
        );
    }

    public static Event toEntity(EventRequestDTO eventRequestDTO) {
        Event event = new Event();
        event.setTitle(eventRequestDTO.title());
        event.setDescription(eventRequestDTO.description());
        event.setStartDateTime(eventRequestDTO.startDateTime());
        event.setEndDateTime(eventRequestDTO.endDateTime());
        event.setCutOffTime(eventRequestDTO.cutOffTime());
        event.setLocation(eventRequestDTO.location());
        event.setSlotsAvailable(eventRequestDTO.slotsAvailable());
        return event;
    }
}
