package com.francis.taratulong.user.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OrgRequestDTO(

        @NotBlank
        @Email
        String email,
        @NotBlank(message = "Name required")
        String name,
        @NotBlank(message = "Description required")
        String description,
        @NotBlank(message = "Location required")
        String location
) {
}
