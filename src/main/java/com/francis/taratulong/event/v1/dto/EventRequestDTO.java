package com.francis.taratulong.event.v1.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequestDTO(

        @NotBlank (message = "Title required")
        String title,
        @NotBlank (message = "Description required")
        String description,
        @NotNull(message = "Start date required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDateTime startDateTime,
        @NotNull (message = "End date required")
        @FutureOrPresent(message = "End date must be today or in the future")
        LocalDateTime endDateTime,
        @NotNull (message = "Cut off time required")
        LocalDateTime cutOffTime,
        @NotBlank (message = "Location required")
        String location,
        @NotNull (message = "Slots required")
        int slotsAvailable

) {
}
