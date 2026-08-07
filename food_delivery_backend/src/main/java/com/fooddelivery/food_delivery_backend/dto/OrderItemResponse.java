package com.fooddelivery.food_delivery_backend.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    String menuItemName,
    Integer quantity,
    BigDecimal priceAtOrder
) {}