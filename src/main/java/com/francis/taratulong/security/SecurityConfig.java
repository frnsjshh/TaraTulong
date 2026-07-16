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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF because we are using stateless JWTs, not browser cookies
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ROUTES ---
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/volunteers/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/organizations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizations/**").permitAll() //Anyone can view the org details


                        // --- PROTECTED ROUTES ---
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // EVENTS
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/**").hasRole("ORG")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasRole("ORG")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ORG")

                        // REGISTRATIONS
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/registrations/**").hasRole("ORG")
                        .requestMatchers(HttpMethod.POST, "/api/v1/registrations/**").hasRole("VOLUNTEER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/registrations/**").hasRole("VOLUNTEER")

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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
