package com.francis.taratulong.event.v1.dto;

import com.francis.taratulong.event.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventResponseDTO toResponseDTO(Event event);
    Event toEntity(EventRequestDTO eventRequestDTO);
}
