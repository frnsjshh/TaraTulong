package com.francis.taratulong.security.v1.dto;

import com.francis.taratulong.user.Role;

public record AuthResponse(
        String token,
        Role role
) {
}
