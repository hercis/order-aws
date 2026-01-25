package org.acme.order.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import org.acme.order.domain.Order;
import org.acme.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class CreateOrderHandlerTest {

  @Inject CreateOrderHandler handler;

  @InjectMock OrderService orderService;

  @Test
  public void testSimpleLambdaSuccess() throws Exception {
    Order order = new Order();
    order.setOrderId("o-123");

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setHttpMethod("POST");
    event.setPath("/order");
    event.setBody(
        """
            {
              "customerId": "c1",
              "items": [
                { "productId": "p1", "quantity": 2, "price": 10.0 }
              ]
            }
        """);

    Mockito.when(orderService.create(Mockito.any())).thenReturn(order);

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

    assertThat(response, notNullValue());
    assertThat(response.getStatusCode(), is(201));
    assertThat(response.getBody(), containsString("o-123"));
  }
}
