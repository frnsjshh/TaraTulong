package com.francis.taratulong.security;

import com.francis.taratulong.user.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
                // Disable CSRF because we are using stateless JWTs, not browser cookies
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ROUTES ---
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/volunteers").permitAll()
                        .requestMatchers(HttpMethod.POST, "/organizations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/organizations").permitAll() //Anyone can view the org details


                        // --- PROTECTED ROUTES ---
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // EVENTS
                        .requestMatchers(HttpMethod.POST, "/events/**").hasRole("ORG")
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("ORG")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ORG")

                        // REGISTRATIONS
                        .requestMatchers(HttpMethod.PATCH, "/registrations/**").hasRole("ORG")
                        .requestMatchers(HttpMethod.POST, "/registrations/**").hasRole("VOLUNTEER")
                        .requestMatchers(HttpMethod.DELETE, "/registrations/**").hasRole("VOLUNTEER")

                        // CATCH-ALL
                        .anyRequest().authenticated() // "Anyone else must be logged in"
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Wire up the password checker and our custom JWT Bouncer
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }



    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

}
