package org.acme.order.mapper;

import org.acme.order.api.CreateOrderRequest;
import org.acme.order.domain.Order;
import org.acme.order.domain.Order.OrderItem;

import java.time.Instant;
import java.util.UUID;

public class OrderMapper {

  public static Order toDomain(CreateOrderRequest request) {
    Order order = new Order();
    order.setOrderId(UUID.randomUUID().toString());
    order.setCustomerId(request.customerId());
    order.setCreatedAt(Instant.now());

    order.setItems(
        request.items().stream()
            .map(
                i -> {
                  OrderItem item = new OrderItem();
                  item.setProductId(i.productId());
                  item.setQuantity(i.quantity());
                  item.setPrice(i.price());
                  return item;
                })
            .toList());
    order.setTotal(
        order.getItems().stream().mapToDouble(i -> i.getQuantity() * i.getPrice()).sum());

    return order;
  }
}
