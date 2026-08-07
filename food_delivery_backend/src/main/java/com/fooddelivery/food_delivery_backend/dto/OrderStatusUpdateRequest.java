package com.fooddelivery.food_delivery_backend.dto;

import com.fooddelivery.food_delivery_backend.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
    @NotNull(message = "Status is required")
    OrderStatus status
) {}
