package com.francis.taratulong.event.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record EventRequestDTO(
        @NotBlank(message = "Organizer required")
        Long organizerId,
        @NotBlank (message = "Title required")
        String title,
        @NotBlank (message = "Description required")
        String description,
        @NotBlank (message = "Start date required")
        LocalDateTime startDateTime,
        @NotBlank (message = "End date required")
        LocalDateTime endDateTime,
        @NotBlank (message = "Cut off time required")
        LocalDateTime cutOffTime,
        @NotBlank (message = "Location required")
        String location,
        @NotBlank (message = "Slots required")
        int slotsAvailable

) {
}
