package com.fooddelivery.food_delivery_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PlaceOrderRequest(
    @NotBlank(message = "Delivery address is required")
    String deliveryAddress,

    @NotEmpty(message = "Order must contain at least one item")
    // @Valid here is important: it tells Spring to also run validation
    // on EACH OrderItemRequest inside this list, not just on the list itself.
    @Valid
    List<OrderItemRequest> items
) {}