package com.orderflow.orders.controller;

import com.orderflow.orders.dto.CreateOrderRequest;
import com.orderflow.orders.dto.OrderResponse;
import com.orderflow.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createdOrder(
            @Valid @RequestBody CreateOrderRequest request
    ){
        return OrderResponse.from(
                orderService.createdOrder(request)
        );
    }

    @GetMapping("/{orderId")
    public OrderResponse getOrder(
            @PathVariable UUID orderId
    ) {
        return OrderResponse.from(
                orderService.getOrder(orderId)
        );
    }
}
