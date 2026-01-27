package org.acme.order.repository;

import org.acme.order.domain.Order;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@ApplicationScoped
public class OrderRepository {

  private final DynamoDbTable<Order> orderTable;

  public OrderRepository(
      DynamoDbEnhancedClient client, @ConfigProperty(name = "order.table-name") String tableName) {
    orderTable = client.table(tableName, TableSchema.fromClass(Order.class));
  }

  public void save(Order order) {
    orderTable.putItem(order);
  }
}
