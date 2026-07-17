package com.francis.taratulong.user.admin.v1.dto;


import com.francis.taratulong.user.admin.Admin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    AdminResponseDTO toResponse(Admin admin);
    Admin toEntity(AdminRequestDTO eventRequestDTO);
    Admin toEntity(AdminRequestUpdateProfile updateProfile);

}
