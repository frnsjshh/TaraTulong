package com.francis.taratulong.location.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record PsgcResponseDTO(
        String code,
        @JsonProperty("area_name") String areaName,
        int reg,
        int prv,
        int mun
) {
}
