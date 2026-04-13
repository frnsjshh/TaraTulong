package com.francis.taratulong.user.volunteer.dto;

import com.francis.taratulong.user.volunteer.Volunteer;

public class VolunteerMapper {
    public static VolunteerResponseDTO toResponseDTO(Volunteer volunteer) {
        return new VolunteerResponseDTO(
                volunteer.getEmail(),
                volunteer.getFirstName(),
                volunteer.getLastName()
        );
    }

    public static Volunteer toEntity(VolunteerRequestDTO volunteerRequestDTO) {
        Volunteer volunteer = new Volunteer();
        volunteer.setEmail(volunteerRequestDTO.email());
        volunteer.setFirstName(volunteerRequestDTO.firstName());
        volunteer.setLastName(volunteerRequestDTO.lastName());
        return volunteer;
    }
}
