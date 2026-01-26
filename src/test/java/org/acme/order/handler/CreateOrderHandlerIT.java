package org.acme.order.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

@QuarkusTest
public class CreateOrderHandlerIT {

  @Inject ObjectMapper mapper;

  @Inject DynamoDbClient dynamoDb;

  @Inject CreateOrderHandler handler;

  @Test
  void shouldPersistOrderInDynamoDb() throws JsonMappingException, JsonProcessingException {
    String body =
        """
          {
            "customerId": "c1",
            "items": [
              { "productId": "p1", "quantity": 2, "price": 10.0 }
            ]
          }
      """;

    APIGatewayProxyRequestEvent event =
        new APIGatewayProxyRequestEvent().withPath("/order").withHttpMethod("POST").withBody(body);
    Context context = mock(Context.class);
    LambdaLogger logger =
        new LambdaLogger() {
          @Override
          public void log(String message) {
            System.out.println(message);
          }

          @Override
          public void log(byte[] message) {
            System.out.println(message == null ? null : new String(message));
          }
        };
    Mockito.when(context.getLogger()).thenReturn(logger);

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

    assertThat(response, notNullValue());
    assertThat(response.getStatusCode(), is(201));
    assertThat(response.getBody(), notNullValue());

    String responseBody = response.getBody();
    Map<String, Object> resultMap =
        mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

    GetItemResponse item =
        dynamoDb.getItem(
            GetItemRequest.builder()
                .tableName("Order")
                .key(Map.of("orderId", AttributeValue.fromS(resultMap.get("orderId").toString())))
                .build());

    assertThat(item.hasItem(), is(true));
  }
}
