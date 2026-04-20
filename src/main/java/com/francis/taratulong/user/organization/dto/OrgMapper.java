package com.francis.taratulong.user.organization.dto;

import com.francis.taratulong.user.organization.Org;

public class OrgMapper {
    public static OrgResponseDTO toResponse(Org org) {
        return new OrgResponseDTO(
                org.getEmail(),
                org.getName(),
                org.getDescription(),
                org.getLocation()
        );
    }

    public static Org toEntity(OrgRequestDTO requestDTO){
        Org org = new Org();
        org.setEmail(requestDTO.email());
        org.setName(requestDTO.name());
        org.setLocation(requestDTO.location());
        org.setDescription(requestDTO.description());
        return org;
    }
}
