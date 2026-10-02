package com.francis.taratulong.user.organization.v1.dto;

import com.francis.taratulong.Status;

public record OrgResponseDTO(
        Long id,
        String email,
        String name,
        String description,
        String location,
        Status status
) {
}
