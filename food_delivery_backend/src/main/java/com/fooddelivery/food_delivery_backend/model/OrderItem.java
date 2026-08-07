package com.fooddelivery.food_delivery_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // The snapshot column we designed all the way back in Phase 1 —
    // this is where that design decision finally gets implemented in code.
    @Column(name = "price_at_order", nullable = false, precision = 8, scale = 2)
    private BigDecimal priceAtOrder;

    protected OrderItem() {}

    public OrderItem(MenuItem menuItem, Integer quantity, BigDecimal priceAtOrder) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public Long getOrderItemId() { return orderItemId; }
    public Order getOrder() { return order; }
    public MenuItem getMenuItem() { return menuItem; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPriceAtOrder() { return priceAtOrder; }

    public void setOrder(Order order) { this.order = order; }
}