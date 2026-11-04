package com.orderflow.orders.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateOrderRequest(
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    String customerId,

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Amount must have at most two decimal places"
    )
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must be a three-letter uppercase code"
    )
    String currency

) {
}
