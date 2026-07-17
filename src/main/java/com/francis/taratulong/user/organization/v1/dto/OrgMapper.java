package com.francis.taratulong.user.organization.v1.dto;

import com.francis.taratulong.user.organization.Org;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrgMapper {
    Org toEntity(OrgRequestDTO requestDTO);
    OrgResponseDTO toResponse(Org org);
}
