package com.francis.taratulong.location.v1;


import com.francis.taratulong.location.v1.dto.PsgcApiResponse;
import com.francis.taratulong.location.v1.dto.PsgcResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    public List<PsgcResponseDTO> fetchProvinces(int regionId) {
        log.info("Fetching provinces from PSGC API");
        try {
            PsgcApiResponse<PsgcResponseDTO> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/provinces")
                            .queryParam("token", apiKey)
                            .queryParam("region", regionId)
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

    public List<PsgcResponseDTO> restRequest(String path, String queryParam, String arg) {
        PsgcApiResponse<PsgcResponseDTO> apiResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("token", apiKey)
                        .queryParam(queryParam, arg)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PsgcApiResponse<PsgcResponseDTO>>() {});
        return (apiResponse != null && apiResponse.results() != null)
                ? apiResponse.results()
                : List.of();
    }






}
