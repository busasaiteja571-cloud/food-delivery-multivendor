package com.fooddelivery.food_delivery_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    String restaurantName,
    String status,
    BigDecimal totalAmount,
    String deliveryAddress,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {}