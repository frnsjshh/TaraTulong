package com.francis.taratulong.location.v1.dto;

import com.francis.taratulong.location.Location;

public class LocationMapper {
    public static Location toEntity(PsgcResponseDTO psgcResponseDTO) {
        Location location = new Location();
        location.setName(psgcResponseDTO.areaName());
        location.setCode(psgcResponseDTO.code());
        location.setRegionId(psgcResponseDTO.reg());
        location.setProvinceId(psgcResponseDTO.prv());
        location.setMunicipalityId(psgcResponseDTO.mun());
        return location;
    }
}
