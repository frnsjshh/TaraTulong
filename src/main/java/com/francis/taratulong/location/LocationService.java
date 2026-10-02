package com.francis.taratulong.location;

import com.francis.taratulong.exception.LocationNotFoundException;
import com.francis.taratulong.location.v1.LocationRestClient;
import com.francis.taratulong.location.v1.dto.LocationMapper;
import com.francis.taratulong.location.v1.dto.PsgcResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationRestClient locationRestClient;

    /**
     * Known PSGC data quality issues: cities/municipalities that the API
     * returns without a proper parent reference. This map defines the
     * correct child → parent relationships using PSGC codes.
     * <p>
     * Key: child PSGC code, Value: correct parent PSGC code.
     * Runs automatically after every PSGC import.
     */
    private static final Map<String, String> PSGC_PARENT_OVERRIDES = Map.ofEntries(
            Map.entry("1430300000", "1401100000"), // City of Baguio → Benguet
            Map.entry("1130700000", "1102400000"), // City of Davao → Davao del Sur
            Map.entry("1030900000", "1003500000"), // City of Iligan → Lanao del Norte
            Map.entry("1030500000", "1004300000"), // City of Cagayan de Oro → Misamis Oriental
            Map.entry("0990101000", "0900700000"), // City of Isabela → Basilan
            Map.entry("0931700000", "0907300000"), // City of Zamboanga → Zamboanga del Sur
            Map.entry("0831600000", "0803700000"), // City of Tacloban → Leyte
            Map.entry("1999908000", "1204700000"), // Tugunan → Cotabato
            Map.entry("1999907000", "1204700000"), // Ligawasan → Cotabato
            Map.entry("1999906000", "1204700000"), // Malidegao → Cotabato
            Map.entry("1999905000", "1204700000"), // Pahamuddin → Cotabato
            Map.entry("1999901000", "1204700000"), // Kapalawan → Cotabato
            Map.entry("1999902000", "1204700000"), // Old Kaabakan → Cotabato
            Map.entry("1999903000", "1204700000"), // Kadayangan → Cotabato
            Map.entry("1999904000", "1908700000"), // Nabalawag → Maguindanao del Norte
            Map.entry("0330100000", "0305400000"), // City of Angeles → Pampanga
            Map.entry("0731300000", "0702200000"), // City of Mandaue → Cebu
            Map.entry("0731100000", "0702200000"), // City of Lapu-Lapu → Cebu
            Map.entry("0730600000", "0702200000"), // City of Cebu → Cebu
            Map.entry("0431200000", "0405600000"), // City of Lucena → Quezon
            Map.entry("0631000000", "0603000000"), // City of Iloilo → Iloilo
            Map.entry("1230800000", "1206300000"), // City of General Santos → South Cotabato
            Map.entry("1830200000", "1804500000"), // City of Bacolod → Negros Occidental
            Map.entry("1630400000", "1600200000"), // City of Butuan → Agusan del Norte
            Map.entry("1731500000", "1705300000"), // City of Puerto Princesa → Palawan
            Map.entry("0331400000", "0307100000")  // City of Olongapo → Zambales
    );

    private void transformAndSaveLocations(List<PsgcResponseDTO> regionsDTO, List<PsgcResponseDTO> provinceDTO, List<PsgcResponseDTO> municipalityDTO) {
        log.info("Attempting to save locations to repository");

        //saving region entities
        List<Location> regionsEntity = regionsDTO.stream().map(
                region -> LocationMapper.toEntity(region, LocationType.REGION)
        ).toList();
        locationRepository.saveAll(regionsEntity);
        log.info("Saved {} regions to repository", regionsEntity.size());

        //creating a region map for fast lookup
        Map<Integer, Location> regionMap = new HashMap<>();
        for(int i = 0; i < regionsEntity.size(); i++) {
            regionMap.put(
                    regionsDTO.get(i).reg(),
                    regionsEntity.get(i)
            );
        }

        //saving province entities
        List<Location> provinceEntity = provinceDTO.stream().map(
                province -> LocationMapper.toEntity(province, LocationType.PROVINCE)
        ).toList();
        for(int i = 0; i < provinceEntity.size(); i++) {
            Location reg = regionMap.get(provinceDTO.get(i).reg());
            if (reg == null) {
                log.warn("Region '{}' not found for province '{}' (PSGC: {}). Parent left unassigned.",
                        provinceDTO.get(i).reg(), provinceDTO.get(i).areaName(), provinceDTO.get(i).code());
            }
            provinceEntity.get(i).setParent(reg);
        }
        log.info("Saved {} provinces to repository", provinceEntity.size());
        locationRepository.saveAll(provinceEntity);

        //creating province map
        Map<Integer, Location> provinceMap = new HashMap<>();
        for(int i = 0; i < provinceEntity.size(); i++) {
            provinceMap.put(
                    provinceDTO.get(i).prv(),
                    provinceEntity.get(i)
            );
        }

        //Convert municipalities and connect to province
        List<Location> municipalityEntity = municipalityDTO.stream().map(
                municipality -> LocationMapper.toEntity(municipality, LocationType.MUNICIPALITY)
        ).toList();
        for(int i = 0; i < municipalityEntity.size(); i++) {
            Location province = provinceMap.get(municipalityDTO.get(i).prv());
            if (province != null) {
                municipalityEntity.get(i).setParent(province);
            } else {
                Location region = regionMap.get(municipalityDTO.get(i).reg());
                if (region != null && region.getCode().equals("1300000000")) {
                    municipalityEntity.get(i).setParent(region);
                } else {
                    log.warn("Province '{}' not found for municipality '{}' (PSGC: {}). Parent left unassigned.",
                            municipalityDTO.get(i).prv(), municipalityDTO.get(i).areaName(), municipalityDTO.get(i).code());
                }
            }

        }
        log.info("Saved {} municipalities to repository", municipalityEntity.size());
        locationRepository.saveAll(municipalityEntity);

        // Fix cities/municipalities that the PSGC API returns without a parent
        fixOrphanedLocations();
    }

    public void fetchAndSaveLocations() {
        log.info("Fetching locations from PSGC API");
        List<PsgcResponseDTO> locations = locationRestClient.fetchAllLocations();
        List<PsgcResponseDTO> regions = new ArrayList<>();
        List<PsgcResponseDTO> provinces = new ArrayList<>();
        List<PsgcResponseDTO> municipalities = new ArrayList<>();

        log.info("Grouping locations by geographic level");
        for (PsgcResponseDTO location : locations) {
            if (location.geographicLevel()==null) {
                log.warn("Error getting geographic level on Location: {}", location.areaName());
                continue;
            }
            switch (location.geographicLevel()) {
                case "Reg" -> regions.add(location);
                case "Prov" -> provinces.add(location);
                case "Mun", "City" -> municipalities.add(location);
            }
        }
        log.info("Saving locations to repository");
        log.info("Region count: {}", regions.size());
        log.info("Province count: {}", provinces.size());
        log.info("Municipality count: {}", municipalities.size());
        transformAndSaveLocations(regions, provinces, municipalities);
    }
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public void deleteAllLocations() {
        locationRepository.deleteAll();
    }

    public Location getLocationById(UUID id) {
        log.info("Getting location by id: {}", id);
        return locationRepository.findById(id).orElseThrow(()-> new LocationNotFoundException("Location not found"));
    }

    public Location getLocationById(UUID id, String errorMsg) {
        log.info("Getting location by id: {}", id);
        return locationRepository.findById(id).orElseThrow(()-> new LocationNotFoundException(errorMsg));
    }

    public Location addParent(UUID id, UUID parentId) {
        log.info("Adding parent to location: id={}, parentId={}", id, parentId);
        Location location = getLocationById(id, "Cannot add parent. Location not found");
        Location parent = getLocationById(parentId, "Cannot add parent. Parent location not found");
        location.setParent(parent);
        log.info("Parent added: id={}, parentId={}", id, parentId);
        return location;
    }

    // ─── PSGC data fixup ───

    /**
     * Fixes orphaned locations from the PSGC API by assigning their correct parents.
     * Uses the PSGC_PARENT_OVERRIDES map. Runs automatically after every import.
     */
    private void fixOrphanedLocations() {
        log.info("Fixing orphaned locations from PSGC data ({} overrides)", PSGC_PARENT_OVERRIDES.size());

        // Collect all codes we need (children + parents) for a single bulk query
        Set<String> allCodes = new HashSet<>();
        allCodes.addAll(PSGC_PARENT_OVERRIDES.keySet());
        allCodes.addAll(PSGC_PARENT_OVERRIDES.values());

        // Bulk fetch and build lookup map
        Map<String, Location> codeMap = new HashMap<>();
        locationRepository.findByCodeIn(allCodes)
                .forEach(loc -> codeMap.put(loc.getCode(), loc));

        int fixed = 0;
        for (Map.Entry<String, String> entry : PSGC_PARENT_OVERRIDES.entrySet()) {
            String childCode = entry.getKey();
            String parentCode = entry.getValue();

            Location child = codeMap.get(childCode);
            Location parent = codeMap.get(parentCode);

            if (child == null) {
                log.warn("Orphan fix skipped: child code '{}' not found in database", childCode);
                continue;
            }
            if (parent == null) {
                log.warn("Orphan fix skipped: parent code '{}' not found in database", parentCode);
                continue;
            }

            child.setParent(parent);
            fixed++;
            log.debug("Fixed orphan: '{}' ({}) → '{}' ({})",
                    child.getName(), childCode, parent.getName(), parentCode);
        }

        log.info("Fixed {}/{} orphaned locations", fixed, PSGC_PARENT_OVERRIDES.size());
    }

    // ─── Hierarchy query methods for frontend cascading dropdowns ───

    /**
     * Returns all regions (top-level locations).
     */
    public List<Location> getRegions() {
        return locationRepository.findByType(LocationType.REGION);
    }

    /**
     * Returns all provinces under a given region.
     */
    public List<Location> getProvincesByRegion(UUID regionId) {
        return locationRepository.findByParentId(regionId);
    }

    /**
     * Returns all cities/municipalities under a given province.
     */
    public List<Location> getCitiesByProvince(UUID provinceId) {
        return locationRepository.findByParentId(provinceId);
    }



}
