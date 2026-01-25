package org.acme.order.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(
    @NotNull String productId, @Positive int quantity, @Positive double price) {}
