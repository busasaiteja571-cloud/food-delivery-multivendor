package com.fooddelivery.food_delivery_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import com.fooddelivery.food_delivery_backend.dto.AuthResponse;
import com.fooddelivery.food_delivery_backend.dto.LoginRequest;
import com.fooddelivery.food_delivery_backend.dto.RegisterRequest;
import com.fooddelivery.food_delivery_backend.model.User;
import com.fooddelivery.food_delivery_backend.repository.UserRepository;
import com.fooddelivery.food_delivery_backend.security.JwtService;

@Service // Marks this as a Spring-managed business-logic bean
public class AuthService {

    // final fields + constructor injection = Spring hands us already-built
    // instances of these three beans automatically. No `new` keyword needed.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        // Business rule: reject duplicate emails BEFORE hitting the database
        // with an insert that would fail on the UNIQUE constraint anyway —
        // this lets us return a clean, specific error message instead of a
        // raw SQL exception leaking to the client.
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // The ONLY place a plain-text password should ever be transformed —
        // encode() runs BCrypt, producing a salted hash. request.password()
        // itself is never stored or logged anywhere.
        String hashedPassword = passwordEncoder.encode(request.password());

        User newUser = new User(
                request.fullName(),
                request.email(),
                hashedPassword,
                request.phoneNumber(),
                request.role()
        );

        User savedUser = userRepository.save(newUser); // INSERT into MySQL happens here
        String token = jwtService.generateToken(savedUser.getEmail(),savedUser.getRole().name());

        return new AuthResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                token
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        // Look up the user by email. If none exists, we deliberately throw
        // the SAME generic error as a wrong password below — never reveal
        // whether it was the email or password that was wrong. This prevents
        // attackers from using login errors to discover which emails are
        // registered on your platform.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // matches() re-hashes the incoming plain-text password using the same
        // salt embedded in the stored hash, then compares the results.
        // The raw password is NEVER decrypted or compared as plain text.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }
}