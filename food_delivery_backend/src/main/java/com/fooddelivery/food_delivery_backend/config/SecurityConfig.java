package com.fooddelivery.food_delivery_backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import static org.springframework.security.config.Customizer.withDefaults;

import com.fooddelivery.food_delivery_backend.security.JwtAuthFilter;
import com.fooddelivery.food_delivery_backend.security.RateLimitFilter;

// @Configuration marks this class as a source of Spring "beans" —
// objects Spring creates once and manages for the whole app's lifetime.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Activates @PreAuthorize / @PostAuthorize on methods
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter; 

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    // @Bean tells Spring: "build this object once, keep it in the
    // application context, and hand it out anywhere it's @Autowired."
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt automatically salts each password differently, so even
        // two users with the identical password get completely different
        // hashes stored in MySQL. It's also deliberately slow — by design —
        // to make brute-force attacks impractical.
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF protection is designed for browser cookie-based sessions.
            // We're building a stateless, token-based API — CSRF doesn't
            // apply here, so we disable it.
            .csrf(csrf -> csrf.disable())
            
            .headers(headers -> headers
            	    .contentTypeOptions(withDefaults())      // prevents MIME-sniffing attacks
            	    .frameOptions(frame -> frame.deny())      // prevents this API being embedded in a hidden <iframe> (clickjacking)
            	)
            
            // Register custom filters in the correct execution order
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            .authorizeHttpRequests(auth -> auth
                // Public: anyone can hit register/login without a token
                .requestMatchers("/api/auth/**").permitAll()
                // Everything else requires a valid, authenticated request
                .anyRequest().authenticated()
            )

            // Tells Spring Security: "never create or use an HTTP session."
            // Every request must prove who it is via its own JWT — the
            // server holds zero memory of "logged in" state between requests.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin)); // Reads from config, not hardcoded
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}