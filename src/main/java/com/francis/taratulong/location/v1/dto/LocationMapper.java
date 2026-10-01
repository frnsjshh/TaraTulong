package com.francis.taratulong.location.v1.dto;

import com.francis.taratulong.location.Location;
import com.francis.taratulong.location.LocationType;

import java.util.UUID;

public class LocationMapper {
    public static Location toEntity(PsgcResponseDTO psgcResponseDTO, LocationType locationType) {
        Location location = new Location();
        location.setName(psgcResponseDTO.areaName());
        location.setCode(psgcResponseDTO.code());
        location.setType(locationType);
        return location;
    }

    /**
     * Builds a LocationResponseDTO with the full hierarchy for a given location.
     */
    public static LocationResponseDTO toResponseDTO(Location location) {
        if (location == null) {
            return null;
        }

        String parentName = null;
        UUID parentId = null;
        String regionName = null;
        UUID regionId = null;

        Location parent = location.getParent();
        if (parent != null) {
            parentName = parent.getName();
            parentId = parent.getId();

            Location grandParent = parent.getParent();
            if (grandParent != null) {
                regionName = grandParent.getName();
                regionId = grandParent.getId();
            } else {
                // Parent is the region itself
                regionName = parent.getName();
                regionId = parent.getId();
            }
        }

        return new LocationResponseDTO(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getType().name(),
                parentName,
                parentId,
                regionName,
                regionId
        );
    }


}
