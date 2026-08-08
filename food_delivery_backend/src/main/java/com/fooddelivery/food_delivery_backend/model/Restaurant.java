package com.fooddelivery.food_delivery_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    // @ManyToOne: MANY restaurants can point to ONE owner (a User).
    // This is the Java-side mirror of the `owner_id` foreign key column
    // we wrote directly in SQL back in Phase 1.
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn tells Hibernate which physical column holds the foreign key.
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 150)
    private String name;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    protected Restaurant() {}

    public Restaurant(User owner, String name, String description, String address) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.address = address;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getRestaurantId() { return restaurantId; }
    public User getOwner() { return owner; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address) { this.address = address; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}