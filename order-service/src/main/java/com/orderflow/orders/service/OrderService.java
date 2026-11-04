package com.orderflow.orders.service;

import com.orderflow.orders.dto.CreateOrderRequest;
import com.orderflow.orders.entity.Order;
import com.orderflow.orders.entity.OrderStatus;
import com.orderflow.orders.messaging.OrderEvent;
import com.orderflow.orders.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object>
    kafkaTemplate;
    private final String orderCreatedTopic;

    public OrderService(
            OrderRepository orderRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}")
            String orderCreatedTopic
    ){
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    @Transactional
    public Order createdOrder(CreateOrderRequest request) {
        Order order = new Order(
                request.customerId(),
                request.amount(),
                request.currency()
        );

        Order savedOrder = orderRepository.save(order);
        /*
          *Educational MVP:
          * publish the event after saving the order.
          *
          * This is not yet atomic with a transactional outbox
          * We will repalce this with a transactional outbox
         */
        kafkaTemplate.send(
                orderCreatedTopic,
                savedOrder.getId().toString(),
                OrderEvent.created(savedOrder)
        );

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId
                ));
    }

    @Transactional
    public Order updateStatus(
            UUID orderId,
            OrderStatus newStatus
    ) {
        Order order = getOrder(orderId);
        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
}
