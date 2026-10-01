package com.francis.taratulong.location;

import com.francis.taratulong.exception.LocationNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;


    @InjectMocks
    private LocationService locationService;

    @Nested
    @DisplayName("getLocationById")
    class GetLocationById {

        @Test
        @DisplayName("should return location if found")
        void shouldReturnLocation() {
            UUID id = UUID.randomUUID();
            Location location = new Location();
            location.setId(id);
            when(locationRepository.findById(id)).thenReturn(Optional.of(location));

            Location result = locationService.getLocationById(id);

            assertEquals(id, result.getId());
        }

        @Test
        @DisplayName("should throw LocationNotFoundException if not found")
        void shouldThrowNotFound() {
            UUID id = UUID.randomUUID();
            when(locationRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(LocationNotFoundException.class, () -> locationService.getLocationById(id));
        }
    }

    @Nested
    @DisplayName("Hierarchy Queries")
    class HierarchyQueries {

        @Test
        @DisplayName("getRegions should return LocationType.REGION")
        void shouldReturnRegions() {
            Location region = new Location();
            region.setType(LocationType.REGION);
            when(locationRepository.findByType(LocationType.REGION)).thenReturn(List.of(region));

            List<Location> regions = locationService.getRegions();

            assertEquals(1, regions.size());
            assertEquals(LocationType.REGION, regions.get(0).getType());
        }

        @Test
        @DisplayName("getProvincesByRegion should return locations with matching parentId")
        void shouldReturnProvinces() {
            UUID regionId = UUID.randomUUID();
            Location province = new Location();
            when(locationRepository.findByParentId(regionId)).thenReturn(List.of(province));

            List<Location> provinces = locationService.getProvincesByRegion(regionId);

            assertEquals(1, provinces.size());
        }

        @Test
        @DisplayName("getCitiesByProvince should return locations with matching parentId")
        void shouldReturnCities() {
            UUID provinceId = UUID.randomUUID();
            Location city = new Location();
            when(locationRepository.findByParentId(provinceId)).thenReturn(List.of(city));

            List<Location> cities = locationService.getCitiesByProvince(provinceId);

            assertEquals(1, cities.size());
        }
    }
}
