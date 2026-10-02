package com.francis.taratulong.event.v1.dto;

import com.francis.taratulong.location.v1.dto.LocationResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;

public record EventResponseDTO(
        Long id,
        String organizerName,
        String title,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime cutOffTime,
        LocationResponseDTO location,
        int slotsAvailable,
        Set<String> categories
) {
}
