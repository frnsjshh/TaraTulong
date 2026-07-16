package com.francis.taratulong.registration.v1.dto;

import com.francis.taratulong.Status;

public record RegistrationResponseDTO(
        Long id,
        Long volunteerId,
        Long eventId,
        Status status,
        Boolean participated,
        Integer rating,
        String feedback
) {
}
