package com.fooddelivery.food_delivery_backend.dto;

import com.fooddelivery.food_delivery_backend.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// A `record` is a perfect fit for a DTO: it's an immutable, data-only
// carrier. No Lombok needed — Java generates the constructor, getters
// (as accessor methods like fullName(), not getFullName()), equals(),
// hashCode(), and toString() for us automatically.
public record RegisterRequest(

    @NotBlank(message = "Full name is required")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password, // plain text ONLY at this stage — never persisted as-is

    String phoneNumber,

    @NotNull(message = "Role is required")
    Role role

) {}