package com.miko.appinstall.controller;

import com.miko.appinstall.model.response.AppResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class WelcomeController {

  public WelcomeController(Router router) {
    router.get("/").handler(this::welcomeHandler);
  }

  private void welcomeHandler(RoutingContext ctx) {
    new AppResponse().success("Welcome! The application is up and running.").send(ctx);
  }
}
