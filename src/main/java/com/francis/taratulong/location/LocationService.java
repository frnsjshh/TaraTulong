package com.francis.taratulong.location;

import com.francis.taratulong.location.v1.dto.LocationMapper;
import com.francis.taratulong.location.v1.dto.PsgcResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;


    public void saveLocations(List<PsgcResponseDTO> regionsDTO, List<PsgcResponseDTO> provinceDTO, List<PsgcResponseDTO> municipalityDTO) {
        //saving region entities
        List<Location> regionsEntity = regionsDTO.stream().map(
                region -> LocationMapper.toEntity(region, LocationType.REGION)
        ).toList();
        locationRepository.saveAll(regionsEntity);

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
                throw new IllegalStateException(
                        "Region not found: " + provinceDTO.get(i).reg()
                );
            }
            provinceEntity.get(i).setParent(reg);
        }
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
            if (province == null) {
                throw new IllegalStateException(
                        "Province not found: " + municipalityDTO.get(i).prv()
                );
            }
            municipalityEntity.get(i).setParent(province);
        }
        locationRepository.saveAll(municipalityEntity);
    }

}
