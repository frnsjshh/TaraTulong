package com.francis.taratulong.registration.dto;

import com.francis.taratulong.registration.Registration;

public final class RegistrationMapper {

    RegistrationMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static RegistrationResponseDTO toResponseDTO(Registration registration) {
        return new RegistrationResponseDTO(
                registration.getId(),
                registration.getVolunteer().getId(),
                registration.getEvent().getId(),
                registration.getRegistrationStatus(),
                registration.getParticipated(),
                registration.getRating(),
                registration.getFeedback()
        );
    }

    public static RatingAndFeedbackRequestAndResponseDTO toRatingAndFeedbackDTO(Registration registration) {
        if (registration.getFeedback()==null) registration.setFeedback("No feedback.");
        return new RatingAndFeedbackRequestAndResponseDTO(
                registration.getRating(),
                registration.getFeedback()
        );
    }
    //No need for mapper for request DTO

}
