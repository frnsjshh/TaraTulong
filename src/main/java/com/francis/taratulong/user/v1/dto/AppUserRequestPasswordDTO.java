package com.francis.taratulong.user.v1.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AppUserRequestPasswordDTO(
        @NotBlank(message = "Current password is required")
        @Length(min = 8, message = "Password must have at least be 8 characters")
        String currentPassword,
        @NotBlank(message = "Password is required")
        @Length(min = 8, message = "Password must have at least be 8 characters")
        String password
) {
}
