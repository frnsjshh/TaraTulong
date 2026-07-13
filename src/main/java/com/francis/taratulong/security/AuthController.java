package com.francis.taratulong.security;


import com.francis.taratulong.security.dto.AuthResponse;
import com.francis.taratulong.security.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse logIn(@Valid@RequestBody LoginRequest loginRequest) {

        return authService.login(loginRequest);
    }
}
