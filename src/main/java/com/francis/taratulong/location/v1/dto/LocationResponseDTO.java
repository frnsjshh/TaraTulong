package com.francis.taratulong.location.v1.dto;

import java.util.UUID;

/**
 * Frontend-friendly location DTO that includes the full hierarchy.
 * Example: { id: "...", name: "Quezon City", type: "CITY", parentName: "Metro Manila", regionName: "NCR" }
 */
public record LocationResponseDTO(
        UUID id,
        String code,
        String name,
        String type,
        String parentName,
        UUID parentId,
        String regionName,
        UUID regionId
) {
}
