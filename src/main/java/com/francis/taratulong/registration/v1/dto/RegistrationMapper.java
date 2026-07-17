package com.francis.taratulong.registration.v1.dto;

import com.francis.taratulong.registration.Registration;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface RegistrationMapper {

    RegistrationResponseDTO toResponseDTO(Registration registration);

    RatingAndFeedbackRequestAndResponseDTO toRatingAndFeedbackDTO(Registration registration);

}
