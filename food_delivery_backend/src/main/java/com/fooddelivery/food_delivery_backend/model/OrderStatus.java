package com.fooddelivery.food_delivery_backend.model;

// Mirrors the ENUM we defined on the `orders.status` column in Phase 1 SQL.
public enum OrderStatus {
    PLACED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}