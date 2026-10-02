package com.francis.taratulong.registration.v1.dto;

import com.francis.taratulong.registration.Registration;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {VolunteerMapper.class})
public interface RegistrationMapper {

    @Mapping(target = "volunteerId", source = "volunteer.id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "status", source = "registrationStatus")
    @Mapping(target = "attendanceStatus", source = "attendanceStatus")
    @Mapping(target = "appliedAt", source = "appliedAt")
    @Mapping(target = "volunteer", source = "volunteer")
    RegistrationResponseDTO toResponseDTO(Registration registration);

    RatingAndFeedbackRequestAndResponseDTO toRatingAndFeedbackDTO(Registration registration);

}
