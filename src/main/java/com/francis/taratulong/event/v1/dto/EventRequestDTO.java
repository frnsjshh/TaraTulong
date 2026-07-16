package com.francis.taratulong.event.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequestDTO(
        @NotNull(message = "Organizer required")
        Long organizerId,
        @NotBlank (message = "Title required")
        String title,
        @NotBlank (message = "Description required")
        String description,
        @NotNull(message = "Start date required")
        LocalDateTime startDateTime,
        @NotNull (message = "End date required")
        LocalDateTime endDateTime,
        @NotNull (message = "Cut off time required")
        LocalDateTime cutOffTime,
        @NotBlank (message = "Location required")
        String location,
        @NotNull (message = "Slots required")
        int slotsAvailable

) {
}
