package com.francis.taratulong.event.v1.dto;

import com.francis.taratulong.category.Category;
import com.francis.taratulong.event.Event;
import com.francis.taratulong.location.Location;
import com.francis.taratulong.location.v1.dto.LocationMapper;
import com.francis.taratulong.location.v1.dto.LocationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "organizerName", source = "organizer.name")
    @Mapping(target = "location", source = "location", qualifiedByName = "toLocationDTO")
    @Mapping(target = "categories", source = "categories", qualifiedByName = "toCategoryNames")
    EventResponseDTO toResponseDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Event toEntity(EventRequestDTO eventRequestDTO);

    @Named("toLocationDTO")
    default LocationResponseDTO toLocationDTO(Location location) {
        return LocationMapper.toResponseDTO(location);
    }

    @Named("toCategoryNames")
    default Set<String> toCategoryNames(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptySet();
        }
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }
}
