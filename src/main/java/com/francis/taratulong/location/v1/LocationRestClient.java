package com.francis.taratulong.location.v1;


import com.francis.taratulong.location.v1.dto.PsgcApiResponse;
import com.francis.taratulong.location.v1.dto.PsgcResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;

import java.util.List;

@Component
@Slf4j
public class LocationRestClient {
    private final RestClient restClient;
    private final String apiKey;

    public LocationRestClient(
            RestClient.Builder builder,
            @Value("${location.api.base-url}") String baseUrl,
            @Value("${location.api.key}") String apiKey
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public List<PsgcResponseDTO> fetchRegions() {
        log.info("Fetching regions from PSGC API");

        try {
            PsgcApiResponse<PsgcResponseDTO> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/regions")
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PsgcApiResponse<PsgcResponseDTO>>() {});
            List<PsgcResponseDTO> regions = (response !=null && response.results() != null)
                    ? response.results()
                    : List.of();
            log.info("Successfully fetched {} regions from PSGC API", regions.size());
            return regions;
        } catch (Exception e) {
            log.error("Error fetching region from PSGC API: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<PsgcResponseDTO> fetchProvinces() {
        log.info("Fetching provinces from PSGC API");
        try {
            PsgcApiResponse<PsgcResponseDTO> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/provinces")
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PsgcApiResponse<PsgcResponseDTO>>() {});

            List<PsgcResponseDTO> provinces = (response != null && response.results() != null)
                    ? response.results()
                    : List.of();
            log.info("Successfully fetched {} provinces from PSGC API", provinces.size());
            return provinces;
        } catch (Exception e) {
            log.error("Error fetching province from PSGC API: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<PsgcResponseDTO> fetchMunicipalities() {
        log.info("Fetching municipalities from PSGC API");
        try {
            List<PsgcResponseDTO> allMunicipalities = new ArrayList<>();

            PsgcApiResponse<PsgcResponseDTO> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/municipalities")
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PsgcApiResponse<PsgcResponseDTO>>() {});

            while (response != null) {
                if (response.results() != null) {
                    allMunicipalities.addAll(response.results());
                }

                String nextUrl = response.next();
                if (nextUrl == null || nextUrl.isBlank()) {
                    break;
                }

                response = restClient.get()
                        .uri(nextUrl)
                        .retrieve()
                        .body(new ParameterizedTypeReference<PsgcApiResponse<PsgcResponseDTO>>() {});
            }

            log.info("Successfully fetched {} municipalities from PSGC API", allMunicipalities.size());
            return allMunicipalities;
        } catch (Exception e) {
            log.error("Error fetching municipality from PSGC API: {}", e.getMessage(), e);
            throw e;
        }
    }







}
