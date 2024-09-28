package com.miko.appinstall.controller;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class WelcomeController {

  public WelcomeController(Router router) {
    router.get("/welcome").handler(this::welcomeHandler);
  }

  private void welcomeHandler(RoutingContext ctx) {
    ctx.response()
      .putHeader("content-type", "text/plain")
      .end("Welcome! The application is up and running.");
  }
}
