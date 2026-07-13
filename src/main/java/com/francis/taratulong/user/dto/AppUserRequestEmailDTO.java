package com.francis.taratulong.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppUserRequestEmailDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
