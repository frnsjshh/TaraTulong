package com.francis.taratulong.user.admin.v1.dto;


import com.francis.taratulong.user.admin.Admin;
import com.francis.taratulong.user.organization.Org;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    AdminResponseDTO toResponse(Admin admin);
    Admin toEntity(AdminRequestDTO adminRequestDTO);
    Admin toEntity(AdminRequestUpdateProfile updateProfile);

    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy.name")
    AdminOrgResponseDTO toAdminOrgResponseDTO(Org org);
}
