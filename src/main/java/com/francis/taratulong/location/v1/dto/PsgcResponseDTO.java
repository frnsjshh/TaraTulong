package com.francis.taratulong.location.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PsgcResponseDTO(
        String code,
        @JsonProperty("area_name") String areaName,
        int reg,
        int prv,
        int mun
) {
}
