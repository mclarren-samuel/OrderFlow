package com.orderflow.orders.entity;

import jakarta.persistence.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Order(){
    }

    public Order(
            String customerId,
            BigDecimal amount,
            String currency
    ){
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = OrderStatus.PENDING;
    }

    @PrePersist
    void onCreate(){
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = Instant.now();
    }

    public UUID getId(){
        return id;
    }

    public String getCustomerId(){
        return customerId;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public String getCurrency(){
        return currency;
    }
    public OrderStatus getStatus(){
        return status;
    }
    public Instant getCreatedAt(){
        return createdAt;
    }
    public Instant getUpdatedAt(){
        return updatedAt;
    }
    public void setStatus(OrderStatus status){
        this.status = status;
    }
}
