package com.francis.taratulong.user.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record VolunteerUpdateProfile(
        @NotBlank(message = "First name required")
        @Length(min = 2, max = 50, message = "First name must be 8-50 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Length(min = 2, max = 50, message = "Last name must be 8-50 characters")
        String lastName
) {
}
