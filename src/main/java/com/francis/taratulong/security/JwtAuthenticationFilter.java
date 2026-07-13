package com.francis.taratulong.security;

import com.francis.taratulong.user.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }



    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // If there is no Authorization header, or it doesn't start with "Bearer ",
        // we just pass the request along. (If they are trying to access a protected route,
        // Spring Security will block them later).
        if(authHeader==null|| !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // The header looks like "Bearer eyJhbGciOiJIUzI1NiJ9...", so we cut off the first 7 characters.
        jwt = authHeader.substring(7);

        try{
            userEmail = jwtService.extractUserName(jwt);

            // We check if we got an email, AND we check if the user is NOT already authenticated in the current security context.
            if(userEmail!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
                UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(userEmail);
                if(jwtService.isTokenValid(jwt, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // We don't put the password here for security reasons
                            userDetails.getAuthorities() // This is where "ROLE_ORG" or "ROLE_VOLUNTEER" gets passed in!
                    );
                    // We attach some extra network details (like their IP address)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 3e. We finally tell Spring Security: "This user is officially authenticated for this request."
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        //Always pass the request to the next filter in the chain!
        filterChain.doFilter(request, response);
    }
}
