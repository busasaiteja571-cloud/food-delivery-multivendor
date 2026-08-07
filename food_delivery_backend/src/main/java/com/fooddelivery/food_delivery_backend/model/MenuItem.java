package com.fooddelivery.food_delivery_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // BigDecimal in Java mirrors DECIMAL(8,2) in MySQL — this is the
    // Java type that gives us the same exact-precision guarantee for
    // money that we designed into the schema back in Phase 1.
    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    protected MenuItem() {}

    public MenuItem(Restaurant restaurant, String name, String description, BigDecimal price) {
        this.restaurant = restaurant;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = true;
    }

    public Long getItemId() { return itemId; }
    public Restaurant getRestaurant() { return restaurant; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Boolean getIsAvailable() { return isAvailable; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}