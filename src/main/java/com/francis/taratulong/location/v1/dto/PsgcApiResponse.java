package com.francis.taratulong.location.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PsgcApiResponse<T>(
        int count,
        String next,
        String previous,
        List<T> results
) {
}
