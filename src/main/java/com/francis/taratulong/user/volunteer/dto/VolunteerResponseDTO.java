package com.francis.taratulong.user.volunteer.dto;

import org.springframework.validation.annotation.Validated;

@Validated
public record VolunteerResponseDTO (
        String email,
        String firstName,
        String lastName
){
}
