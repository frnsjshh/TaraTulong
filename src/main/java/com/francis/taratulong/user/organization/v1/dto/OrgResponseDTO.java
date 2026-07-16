package com.francis.taratulong.user.organization.v1.dto;

public record OrgResponseDTO(
        String email,
        String name,
        String description,
        String location
) {
}
