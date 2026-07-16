package com.francis.taratulong.registration.v1.dto;

import jakarta.validation.constraints.NotNull;

public record RegistrationRequestDTO(
        @NotNull(message = "Event id required")
        Long eventId
) {
}
