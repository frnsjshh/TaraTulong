package com.francis.taratulong.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginRequest(
        @NotBlank (message = "Email is required")
        @Email (message = "Must be a valid email format")
        String email,

        @NotBlank (message = "Password is required")
        @Length (min = 8, message = "Password must have at least be 8 characters")
        String password
) {
}
