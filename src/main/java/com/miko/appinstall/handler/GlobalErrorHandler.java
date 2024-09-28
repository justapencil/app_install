package com.miko.appinstall.handler;

import com.miko.appinstall.model.response.AppResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class GlobalErrorHandler {

  public static void handleFailure(RoutingContext ctx) {
    Throwable failure = ctx.failure();
    int statusCode = ctx.statusCode() != -1 ? ctx.statusCode() : 500;

    log.error("Failed to handle request", failure);
    Map<String, String> response = Map.of("error", failure.getMessage());

    new AppResponse()
      .status(statusCode)
      .contentType("application/json")
      .body(response)
      .send(ctx);
  }
}
