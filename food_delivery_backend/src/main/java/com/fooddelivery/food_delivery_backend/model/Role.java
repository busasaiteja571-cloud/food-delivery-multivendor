package com.fooddelivery.food_delivery_backend.model;

// Mirrors the ENUM('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_AGENT', 'ADMIN')
// column we defined in MySQL. Using a Java enum instead of a raw String
// gives us compile-time safety — you can't accidentally typo "CUSTOMR".
public enum Role {
    CUSTOMER,
    RESTAURANT_OWNER,
    DELIVERY_AGENT,
    ADMIN
}