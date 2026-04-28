package com.francis.taratulong.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrationRequestDTO(
        @NotBlank(message = "Volunteer id required")
        Long volunteerId,
        @NotBlank(message = "Event id required")
        Long eventId
) {
}
