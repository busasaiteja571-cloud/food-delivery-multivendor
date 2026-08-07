package com.fooddelivery.food_delivery_backend.dto;

// What we send BACK to the client after successful login/registration.
// Notice: no passwordHash field exists here at all — it's structurally
// impossible to leak it, because this record simply has no field for it.
public record AuthResponse(
    Long userId,
    String fullName,
    String email,
    String role,
    String token // the JWT — we'll generate this in the next security step
) {}