package com.francis.taratulong.security;

import com.francis.taratulong.security.v1.dto.AuthResponse;
import com.francis.taratulong.security.v1.dto.LoginRequest;
import com.francis.taratulong.user.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for email={}", request.email());
        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
            );

        AppUser user = (AppUser) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(user);
        log.info("Login successful: email={}, role={}", user.getEmail(), user.getRole());
        return new AuthResponse(jwtToken);
    }
}
