package com.francis.taratulong.user.admin.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AdminRequestDTO(
        @NotBlank(message = "Email required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Name required")
        String name,
        @NotBlank(message = "Password required")
        @Length(min = 8, message = "Password must have at least be 8 characters")
        String password
) {
}
