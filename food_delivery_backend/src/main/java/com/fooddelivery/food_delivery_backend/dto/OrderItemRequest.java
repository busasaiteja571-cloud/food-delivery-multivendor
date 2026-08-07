package com.fooddelivery.food_delivery_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Deliberately contains ONLY an ID and a quantity — no price field exists
// here at all, so there's nothing for a malicious client to tamper with.
public record OrderItemRequest(
    @NotNull(message = "Menu item ID is required")
    Long menuItemId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}