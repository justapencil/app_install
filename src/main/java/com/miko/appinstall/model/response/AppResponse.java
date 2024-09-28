package com.miko.appinstall.model.response;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class AppResponse<T> {

  private int statusCode;
  private T body;
  private String contentType;
  private Map<String, String> headers;

  public AppResponse() {
    this.statusCode = 200;
    this.headers = new HashMap<>();
    this.contentType = "text/plain";
  }

  public AppResponse<T> status(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public AppResponse<T> body(T body) {
    this.body = body;
    return this;
  }

  public AppResponse<T> contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public AppResponse<T> header(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public AppResponse<T> json(T body) {
    this.body = body;
    this.contentType = "application/json";
    return this;
  }

  public void send(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.setStatusCode(statusCode);
    response.putHeader("content-type", contentType);
    headers.forEach(response::putHeader);

    if (body != null) {
      if (contentType.equals("application/json")) {
        response.end(Json.encodePrettily(body));
      } else {
        response.end(body.toString());
      }
    } else {
      response.end();
    }
  }

  public void sendError(RoutingContext ctx, int statusCode, String message) {
    this.statusCode = statusCode;
    this.body = (T) Map.of("error", message);
    this.contentType = "application/json";
    this.send(ctx);
  }

  public AppResponse<T> success(String s) {
    this.statusCode = 200;
    this.body = (T) Map.of("message", s);
    this.contentType = "application/json";
    return this;
  }
}
