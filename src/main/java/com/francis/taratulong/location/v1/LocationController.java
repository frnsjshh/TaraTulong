package com.francis.taratulong.location.v1;


import com.francis.taratulong.location.Location;
import com.francis.taratulong.location.LocationService;
import com.francis.taratulong.location.v1.dto.LocationMapper;
import com.francis.taratulong.location.v1.dto.LocationResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Location")
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<Void> updateLocation() {
        locationService.fetchAndSaveLocations();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllLocations() {
        locationService.deleteAllLocations();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations() {
        List<LocationResponseDTO> locations = locationService.getAllLocations().stream()
                .map(LocationMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(locations);
    }

    /**
     * Returns all regions (top-level). Use as the first step in cascading dropdowns.
     */
    @GetMapping("/regions")
    public ResponseEntity<List<LocationResponseDTO>> getRegions() {
        List<LocationResponseDTO> regions = locationService.getRegions().stream()
                .map(LocationMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(regions);
    }

    /**
     * Returns all provinces under a given region.
     */
    @GetMapping("/provinces")
    public ResponseEntity<List<LocationResponseDTO>> getProvincesByRegion(
            @RequestParam UUID regionId
    ) {
        List<LocationResponseDTO> provinces = locationService.getProvincesByRegion(regionId).stream()
                .map(LocationMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(provinces);
    }

    /**
     * Returns all cities/municipalities under a given province.
     */
    @GetMapping("/cities")
    public ResponseEntity<List<LocationResponseDTO>> getCitiesByProvince(
            @RequestParam UUID provinceId
    ) {
        List<LocationResponseDTO> cities = locationService.getCitiesByProvince(provinceId).stream()
                .map(LocationMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(cities);
    }

    /**
     * Returns a single location with its full hierarchy.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> getLocationById(@PathVariable UUID id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(LocationMapper.toResponseDTO(location));
    }

    @PutMapping("/addparent")
    public ResponseEntity<LocationResponseDTO> addParent(
            @RequestParam UUID id,
            @RequestParam UUID parentId
    ) {
        Location location = locationService.addParent(id, parentId);
        return ResponseEntity.ok(LocationMapper.toResponseDTO(location));
    }
}
