package com.francis.taratulong.user.admin.dto;

import com.francis.taratulong.user.admin.Admin;

public final class AdminMapper {
    public static AdminResponseDTO toResponse(Admin admin){
        return new AdminResponseDTO(
                admin.getEmail(),
                admin.getName()
        );
    }

    public static Admin toEntity(AdminRequestDTO adminRequestDTO) {
        Admin admin = new Admin();
        admin.setName(adminRequestDTO.name());
        admin.setEmail(adminRequestDTO.email());
        return admin;
    }
}
