package com.orderflow.orders.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orderflow.orders.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEvent (
        UUID eventId,
        UUID orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String status,
        Instant occurredAt,
        String reason
){

    public static OrderEvent created(Order order){
        return new OrderEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus().name(),
                Instant.now(),
                null
        );
    }
    public static OrderEvent statusChanged(
            Order order,
            String reason
    ){
        return new OrderEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus().name(),
                Instant.now(),
                reason
        );
    }
}
