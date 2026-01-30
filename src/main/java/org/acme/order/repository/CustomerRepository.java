package org.acme.order.repository;

import java.util.Map;
import java.util.Optional;

import org.acme.order.domain.Customer;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository {

  private static final Map<String, Customer> CUSTOMERS =
      Map.of("c1", new Customer("c1", "Eduardo"), "c2", new Customer("c2", "Jose"));

  public Optional<Customer> findById(String customerId) {
    return Optional.ofNullable(CUSTOMERS.get(customerId));
  }
}
