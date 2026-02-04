package org.acme.order.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotNull String customerId, @Valid @NotEmpty List<CreateOrderItemRequest> items) {}
