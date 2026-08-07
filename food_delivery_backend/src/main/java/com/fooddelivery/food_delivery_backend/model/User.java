package com.fooddelivery.food_delivery_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// @Entity tells Hibernate: "this class represents a database table."
@Entity
// @Table lets us explicitly name the table, so the class name (User)
// doesn't have to exactly match the table name (users).
@Table(name = "users")
public class User {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY tells Hibernate: "let MySQL's AUTO_INCREMENT handle ID generation,
    // don't generate IDs yourself." This matches how we defined the column in SQL.
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // Deliberately named passwordHash, not password — a reminder to every
    // future developer touching this class that this field NEVER holds
    // a plain-text password, only the hashed output of BCrypt.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    // STRING (not ORDINAL) tells Hibernate to store "CUSTOMER" as text in MySQL,
    // not the number 0. If we ever reorder the enum, ORDINAL would silently
    // corrupt existing data — STRING is always the safer choice.
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // JPA requires a no-argument constructor so Hibernate can instantiate
    // the object via reflection before populating its fields.
    protected User() {}

    public User(String fullName, String email, String passwordHash, String phoneNumber, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Standard getters — no Lombok, so we write these explicitly.
    // No setters for userId/createdAt/email: identity fields shouldn't be
    // freely mutable after creation.
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}