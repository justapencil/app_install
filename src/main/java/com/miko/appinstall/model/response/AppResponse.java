package com.miko.appinstall.model.response;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class AppResponse {

  private int statusCode;
  private String body;
  private String contentType;
  private Map<String, String> headers;

  public AppResponse() {
    this.statusCode = 200;
    this.headers = new HashMap<>();
    this.contentType = "text/plain";
  }

  public AppResponse status(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }
  public AppResponse body(String body) {
    this.body = body;
    return this;
  }

  public AppResponse contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public AppResponse header(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public void send(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.setStatusCode(statusCode);
    response.putHeader("content-type", contentType);
    headers.forEach(response::putHeader);

    if (body != null) {
      response.end(body);
    } else {
      response.end();
    }
  }
}
