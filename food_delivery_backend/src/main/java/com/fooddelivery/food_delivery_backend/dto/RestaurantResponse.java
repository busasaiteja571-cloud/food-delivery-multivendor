package com.fooddelivery.food_delivery_backend.dto;

// Deliberately excludes the owner's User object entirely — a customer
// browsing restaurants has no need to see the owner's account details,
// and we NEVER want a passwordHash reachable through any serialization path.
public record RestaurantResponse(
    Long restaurantId,
    String name,
    String description,
    String address,
    Boolean isActive,
    String imageUrl
) {}