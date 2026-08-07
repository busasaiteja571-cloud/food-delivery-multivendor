package com.fooddelivery.food_delivery_backend.dto;

import java.math.BigDecimal;

public record MenuItemResponse(
    Long itemId,
    String name,
    String description,
    BigDecimal price,
    Boolean isAvailable
) {}