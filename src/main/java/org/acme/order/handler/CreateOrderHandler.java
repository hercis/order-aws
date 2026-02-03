package org.acme.order.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.acme.order.api.CreateOrderRequest;
import org.acme.order.domain.Order;
import org.acme.order.service.OrderService;
import org.acme.support.AppError;
import org.acme.support.Result;
import org.acme.support.AppError.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

// import jakarta.json.Json;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
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
    if (event.getHttpMethod().equals(SdkHttpMethod.OPTIONS.name())) {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(HttpStatusCode.OK)
          .withHeaders(corsHeaders());
    }
    if (!event.getHttpMethod().equals(SdkHttpMethod.POST.name())) {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(HttpStatusCode.METHOD_NOT_ALLOWED)
          .withBody("Only POST method is supported");
    }

    try {
      CreateOrderRequest request = mapper.readValue(event.getBody(), CreateOrderRequest.class);

      Set<ConstraintViolation<Object>> violations = validator.validate(request);
      if (!violations.isEmpty()) {
        Map<String, String> errors =
            violations.stream()
                .collect(
                    Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage()));
        return new APIGatewayProxyResponseEvent()
            .withStatusCode(400)
            .withBody(violations.toString());
        // .withBody(mapper.writeValueAsString(errors));
      }

      Result<Order, AppError> serviceResult = service.create(request);
      Result<String, AppError> mappingOutput =
          serviceResult.flatMap(
              order ->
                  Result.tryOf(() -> mapper.writeValueAsString(order))
                      .mapError(ex -> InternalAppError.fromCause("json", ex)));

      APIGatewayProxyResponseEvent response =
          mappingOutput.fold(
              error ->
                  switch (error) {
                    case NotFoundError e ->
                        new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatusCode.NOT_FOUND)
                            .withBody(buildBody(e))
                            .withHeaders(corsHeaders(Map.of("Content-Type", "application/json")));
                    case AppError e ->
                        new APIGatewayProxyResponseEvent()
                            .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                            .withBody(buildBody(e))
                            .withHeaders(corsHeaders(Map.of("Content-Type", "application/json")));
                  },
              json ->
                  new APIGatewayProxyResponseEvent()
                      .withStatusCode(HttpStatusCode.CREATED)
                      .withBody(json)
                      .withHeaders(corsHeaders(Map.of("Content-Type", "application/json"))));
      return response;
    } catch (Exception e) {
      context.getLogger().log(e.getMessage());
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
          .withHeaders(corsHeaders(Map.of("Content-Type", "application/json")))
          .withBody("{\"error\":\"Internal server error\"}");
    }
  }

  private String buildBody(AppError error) {
    return mapper.createObjectNode().put("message", error.message()).toString();
  }

  private Map<String, String> corsHeaders() {
    return corsHeaders(Map.of());
  }

  private Map<String, String> corsHeaders(Map<String, String> additionalHeaders) {
    Map<String, String> headers = new HashMap<>(additionalHeaders);
    headers.put("Access-Control-Allow-Origin", "http://localhost:5173");
    headers.put("Access-Control-Allow-Methods", "OPTIONS,PUT");
    headers.put("Access-Control-Allow-Headers", "Content-Type,Accept");
    headers.put("Access-Control-Allow-Credentials", "true");
    return headers;
  }
}
