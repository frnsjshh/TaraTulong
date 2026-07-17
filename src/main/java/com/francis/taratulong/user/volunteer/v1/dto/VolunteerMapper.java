package com.francis.taratulong.user.volunteer.v1.dto;

import com.francis.taratulong.user.volunteer.Volunteer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VolunteerMapper {
    VolunteerResponseDTO toResponseDTO(Volunteer volunteer);

    Volunteer toEntity(VolunteerRequestDTO volunteerRequestDTO);

    Volunteer toEntity(VolunteerRequestProfileDTO volunteerRequestProfileDTO);


}
