package com.fooddelivery.food_delivery_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RestaurantRequest(
    @NotBlank(message = "Restaurant name is required")
    String name,

    String description,

    @NotBlank(message = "Address is required")
    String address
) {}