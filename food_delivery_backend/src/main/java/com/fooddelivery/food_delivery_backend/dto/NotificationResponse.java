package com.fooddelivery.food_delivery_backend.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long orderId,
    String message,
    LocalDateTime occurredAt
) {}