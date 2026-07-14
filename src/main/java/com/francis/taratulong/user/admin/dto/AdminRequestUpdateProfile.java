package com.francis.taratulong.user.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminRequestUpdateProfile (
        @NotBlank(message = "Name required")
        String name
) {
}
