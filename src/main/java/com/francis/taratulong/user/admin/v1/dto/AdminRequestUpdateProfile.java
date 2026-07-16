package com.francis.taratulong.user.admin.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminRequestUpdateProfile (
        @NotBlank(message = "Name required")
        String name
) {
}
