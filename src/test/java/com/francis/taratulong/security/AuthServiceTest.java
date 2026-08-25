package com.francis.taratulong.security;

import com.francis.taratulong.security.v1.dto.AuthResponse;
import com.francis.taratulong.security.v1.dto.LoginRequest;
import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.AppUserRepository;
import com.francis.taratulong.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AppUser appUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail("user@email.com");
        appUser.setPassword("encodedPassword");
        appUser.setRole(Role.VOLUNTEER);

        // LoginRequest is a record — we construct it directly
        loginRequest = new LoginRequest("user@email.com", "rawPassword");
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return AuthResponse with JWT token on successful login")
        void happyPath() {
            // ARRANGE
            // authenticationManager.authenticate() returns void on success
            // (it throws on failure), so we don't need when(...).thenReturn(...)
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(appUser);
            when(jwtService.generateToken(appUser)).thenReturn("jwt.token.here");

            // ACT
            AuthResponse response = authService.login(loginRequest);

            // ASSERT
            assertNotNull(response);
            assertEquals("jwt.token.here", response.token());

            // Verify the authentication manager was actually called
            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            );
            verify(jwtService).generateToken(appUser);
        }


        @Test
        @DisplayName("should throw BadCredentialsException when credentials are invalid")
        void shouldThrowWhenBadCredentials() {
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            assertThrows(
                    BadCredentialsException.class,
                    () -> authService.login(loginRequest)
            );

            // Verify we never tried to fetch the user or generate a token
            verify(appUserRepository, never()).findByEmail(any());
            verify(jwtService, never()).generateToken(any());
        }
    }
}
