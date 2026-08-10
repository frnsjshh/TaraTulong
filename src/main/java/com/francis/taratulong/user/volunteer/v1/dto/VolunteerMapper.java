package com.francis.taratulong.user.volunteer.v1.dto;

import com.francis.taratulong.user.volunteer.Volunteer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface VolunteerMapper {

    @Mapping(target = "trustTier", source = "trustScore", qualifiedByName = "tierCalculator")
    VolunteerResponseDTO toResponseDTO(Volunteer volunteer);

    Volunteer toEntity(VolunteerRequestDTO volunteerRequestDTO);

    Volunteer toEntity(VolunteerRequestProfileDTO volunteerRequestProfileDTO);


    @Named("tierCalculator")
    static String calculateTier(int points) {
        if (points >= 100) return "Platinum (Highly Reliable)";
        if (points >= 75)  return "Gold";
        if (points >= 50)  return "Silver";
        if (points >= 25)  return "Bronze";
        return "High Risk";
    }
}
