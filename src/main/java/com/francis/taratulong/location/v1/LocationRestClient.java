package com.francis.taratulong.location.v1;


import com.francis.taratulong.location.v1.dto.PsgcApiResponse;
import com.francis.taratulong.location.v1.dto.PsgcRegionResponseDTO;
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

    public List<PsgcRegionResponseDTO> fetchRegion() {
        log.info("Fetching region from PSGC API");

        try {
            PsgcApiResponse<PsgcRegionResponseDTO> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/regions")
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PsgcApiResponse<PsgcRegionResponseDTO>>() {});
            List<PsgcRegionResponseDTO> regions = (response !=null && response.results() != null)
                    ? response.results()
                    : List.of();
            log.info("Successfully fetched {} regions from PSGC API", regions.size());
            return regions;
        } catch (Exception e) {
            log.error("Error fetching region from PSGC API: {}", e.getMessage(), e);
            throw e;
        }
    }

}
