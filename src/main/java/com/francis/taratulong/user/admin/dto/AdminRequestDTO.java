package com.francis.taratulong.user.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminRequestDTO(
        @NotBlank(message = "Email required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Name required")
        String name
) {
}
