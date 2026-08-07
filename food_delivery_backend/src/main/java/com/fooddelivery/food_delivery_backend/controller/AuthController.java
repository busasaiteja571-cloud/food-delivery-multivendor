package com.fooddelivery.food_delivery_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.food_delivery_backend.dto.AuthResponse;
import com.fooddelivery.food_delivery_backend.dto.LoginRequest;
import com.fooddelivery.food_delivery_backend.dto.RegisterRequest;
import com.fooddelivery.food_delivery_backend.service.AuthService;

import jakarta.validation.Valid;

// @RestController = @Controller + @ResponseBody combined: every method's
// return value is automatically serialized to JSON in the HTTP response body.
@RestController
// @RequestMapping sets a shared URL prefix for every endpoint in this class.
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @PostMapping maps this method to POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            // @RequestBody deserializes incoming JSON into a RegisterRequest.
            // @Valid triggers our @NotBlank/@Email/@Size checks automatically —
            // if any fail, Spring returns a 400 before this method body even runs.
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        // 201 Created is the correct HTTP status for "a new resource was made"
        return ResponseEntity.status(201).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        // 200 OK, not 201 — we didn't create anything new, just verified identity
        return ResponseEntity.ok(response);
    }
}