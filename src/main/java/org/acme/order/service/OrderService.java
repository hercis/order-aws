package org.acme.order.service;

import org.acme.order.api.CreateOrderRequest;
import org.acme.order.domain.Order;
import org.acme.order.mapper.OrderMapper;
import org.acme.order.repository.OrderRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderService {

  @Inject OrderRepository repository;

  public Order create(CreateOrderRequest request) {
    Order order = OrderMapper.toDomain(request);
    repository.save(order);
    return order;
  }
}
