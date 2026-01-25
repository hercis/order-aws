package org.acme.order.handler;

import java.util.Map;

import org.acme.order.api.CreateOrderRequest;
import org.acme.order.service.OrderService;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Validator;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.http.SdkHttpMethod;

@Named("createOrder")
public class CreateOrderHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject ObjectMapper mapper;
  @Inject Validator validator;
  @Inject OrderService service;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent event, Context context) {
    if (!event.getHttpMethod().equals(SdkHttpMethod.POST.name())) {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(HttpStatusCode.METHOD_NOT_ALLOWED)
          .withBody("Only POST method is supported");
    }

    try {
      CreateOrderRequest request = mapper.readValue(event.getBody(), CreateOrderRequest.class);

      var violations = validator.validate(request);
      if (!violations.isEmpty()) {
        return new APIGatewayProxyResponseEvent()
            .withStatusCode(400)
            .withBody(violations.toString());
      }

      var order = service.create(request);

      return new APIGatewayProxyResponseEvent()
          .withStatusCode(HttpStatusCode.CREATED)
          .withBody(mapper.writeValueAsString(Map.of("orderId", order.getOrderId())))
          .withHeaders(Map.of("Content-Type", "application/json"));
    } catch (Exception e) {
      context.getLogger().log(e.getMessage());
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
          .withBody("{\"error\":\"Internal server error\"}");
    }
  }
}
