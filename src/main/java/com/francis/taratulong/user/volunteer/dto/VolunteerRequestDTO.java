package com.francis.taratulong.user.volunteer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;


public record VolunteerRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "First name is required")
        @Length(min = 2, max = 50, message = "First name must be 8-50 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Length(min = 2, max = 50, message = "Last name must be 8-50 characters")
        String lastName,
        @NotBlank(message = "Password is required")
        @Length(min = 8, message = "Password must have at least be 8 characters")
        String password

) {
}
