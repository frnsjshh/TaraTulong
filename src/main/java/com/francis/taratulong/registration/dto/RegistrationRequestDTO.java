package com.francis.taratulong.registration.dto;

import jakarta.validation.constraints.NotNull;

public record RegistrationRequestDTO(
        @NotNull(message = "Event id required")
        Long eventId
) {
}
