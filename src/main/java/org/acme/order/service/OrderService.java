package org.acme.order.service;

import org.acme.order.api.CreateOrderRequest;
import org.acme.order.domain.Customer;
import org.acme.order.domain.Order;
import org.acme.order.mapper.OrderMapper;
import org.acme.order.repository.CustomerRepository;
import org.acme.order.repository.OrderRepository;
import org.acme.support.AppError;
import org.acme.support.Result;
import org.acme.support.AppError.NotFoundError;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderService {

  @Inject OrderRepository repository;

  @Inject CustomerRepository customerRepository;

  public Result<Order, AppError> create(CreateOrderRequest request) {
    String customerId = request.customerId();
    Result<Order, AppError> result =
        customerRepository
            .findById(customerId)
            .<Result<Customer, AppError>>map(Result::success)
            .orElseGet(
                () -> Result.failure(NotFoundError.fromId("customer.id", "Customer", customerId)))
            .map(
                customer -> {
                  Order order = OrderMapper.toDomain(request);
                  repository.save(order);
                  return order;
                });
    return result;
  }
}
