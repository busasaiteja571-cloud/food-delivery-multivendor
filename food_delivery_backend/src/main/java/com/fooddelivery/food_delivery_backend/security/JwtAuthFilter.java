package com.fooddelivery.food_delivery_backend.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// OncePerRequestFilter guarantees this filter's logic runs exactly once
// per incoming request, no matter how Spring internally forwards/dispatches it.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No "Bearer <token>" header at all? Just let the request continue
        // unauthenticated — the SecurityFilterChain (next step) decides
        // whether this specific URL actually requires authentication.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix (7 characters) to get the raw token string
        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            // Only proceed if we extracted an email AND no authentication
            // has already been set for this request (avoids redundant work).
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(token)) {

                	String role = jwtService.extractRole(token);
                	//Spring Secrity's convention: role-based checks like
                	//hasRole('ADMIN') actually look for an authority
                	//literally named "ROLE_ADMIN" - the "ROLE_" prefix is
                	//added automatically by hasRole() when checking, or
                	//the two will never match
                	var authoritie = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                	
                    // This object is how we tell Spring Security "this request
                    // IS authenticated, as this user." We pass an empty
                    // credentials field (null) since we already proved
                    // identity via the JWT signature, not a password.
                    var authToken = new UsernamePasswordAuthenticationToken(
                            email, null, authoritie
                    );

                    // Registering it here makes the rest of the request
                    // pipeline (and your Controllers, if needed) treat this
                    // request as coming from a logged-in user.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid/tampered/expired token: we simply don't authenticate.
            // We deliberately do NOT throw here — we let the request continue
            // unauthenticated, and let the SecurityFilterChain reject it
            // with a clean 403 if the endpoint requires auth.
        }

        filterChain.doFilter(request, response);
    }
}