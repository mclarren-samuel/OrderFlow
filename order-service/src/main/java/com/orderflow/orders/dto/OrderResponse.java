package com.orderflow.orders.dto;

import com.orderflow.orders.entity.Order;
import com.orderflow.orders.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        BigDecimal amount,
        String currency,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
