package com.francis.taratulong.user.volunteer.v1.dto;

import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
public record VolunteerResponseDTO (
        String email,
        String firstName,
        String lastName,
        BigDecimal attendancePercentage,
        Integer totalEventsAttended
){
}
