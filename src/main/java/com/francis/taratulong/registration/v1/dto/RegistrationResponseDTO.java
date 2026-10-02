package com.francis.taratulong.registration.v1.dto;

import com.francis.taratulong.Status;
import com.francis.taratulong.registration.AttendanceStatus;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerResponseDTO;

import java.time.LocalDateTime;

public record RegistrationResponseDTO(
        Long id,
        Long volunteerId,
        Long eventId,
        Status status,
        AttendanceStatus attendanceStatus,
        LocalDateTime appliedAt,
        Integer rating,
        String feedback,
        VolunteerResponseDTO volunteer
) {
}
