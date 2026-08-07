package com.fooddelivery.food_delivery_backend.dto;

// A deliberately MINIMAL view of a user for admin listing — still no
// passwordHash, still no phone number exposed in bulk lists unless an
// admin specifically needs it later. Least-privilege applies even to admins.
public record UserSummaryResponse(
    Long userId,
    String fullName,
    String email,
    String role
) {}