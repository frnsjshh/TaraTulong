package com.francis.taratulong.user.admin.v1.dto;

import com.francis.taratulong.Status;

import java.time.LocalDateTime;

public record AdminOrgResponseDTO(
        Long id,
        String name,
        String email,
        String description,
        String location,
        Status status,
        Long approvedById,
        String approvedByName,
        LocalDateTime joinDate
) {
}
