package com.francis.taratulong.event.dto;

import java.time.LocalDateTime;

public record EventResponseDTO(
        String organizerName,
        String title,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime cutOffTime,
        String location,
        int slotsAvailable
) {
}
