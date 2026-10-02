package com.francis.taratulong.user.organization.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record OrgRequestDTO(

        @NotBlank
        @Email
        String email,
        @NotBlank(message = "Name required")
        String name,
        @NotBlank(message = "Description required")
        String description,
        @NotBlank(message = "Location required")
        String location,
        @NotBlank(message = "Password required")
        @Length(min = 8, message = "Password must have at least be 8 characters")
        String password
) {
}
