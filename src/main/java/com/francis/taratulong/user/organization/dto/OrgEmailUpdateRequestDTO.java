package com.francis.taratulong.user.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OrgEmailUpdateRequestDTO(
        @NotBlank(message = "Email required")
        @Email(message = "Invalid email format")
        String email
) {
}
