package com.francis.taratulong.location.v1.dto;

import com.francis.taratulong.location.Location;
import com.francis.taratulong.location.LocationType;

public class LocationMapper {
    public static Location toEntity(PsgcResponseDTO psgcResponseDTO, LocationType locationType) {
        Location location = new Location();
        location.setName(psgcResponseDTO.areaName());
        location.setCode(psgcResponseDTO.code());
        location.setType(locationType);
        return location;
    }


}
