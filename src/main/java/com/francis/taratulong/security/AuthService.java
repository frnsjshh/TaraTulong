package com.francis.taratulong.security;

import com.francis.taratulong.security.v1.dto.AuthResponse;
import com.francis.taratulong.security.v1.dto.LoginRequest;
import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;


    public AuthService(AuthenticationManager authenticationManager, AppUserRepository appUserRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AppUser user = appUserRepository.findByEmail(request.email()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
