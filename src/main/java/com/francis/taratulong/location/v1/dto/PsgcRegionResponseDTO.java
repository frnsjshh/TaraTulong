package com.francis.taratulong.location.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PsgcRegionResponseDTO(
        String code,
        @JsonProperty("area_name") String areaName,
        Integer reg,
        @JsonProperty("island_region") String islandRegion
) {
}
