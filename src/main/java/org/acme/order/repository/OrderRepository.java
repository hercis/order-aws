package org.acme.order.repository;

import org.acme.order.domain.Order;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@ApplicationScoped
public class OrderRepository {

  private final DynamoDbTable<Order> orderTable;

  public OrderRepository(DynamoDbEnhancedClient client) {
    orderTable =
        client.table(System.getenv("ORDER_TABLE_NAME"), TableSchema.fromClass(Order.class));
  }

  public void save(Order order) {
    orderTable.putItem(order);
  }
}
