package com.miko.appinstall.controller;

import com.miko.appinstall.annotation.RouteMapping;
import com.miko.appinstall.annotation.RouteController;
import com.miko.appinstall.model.response.AppResponse;
import io.vertx.ext.web.RoutingContext;

@RouteController(path = "/")
public class WelcomeController {

  @RouteMapping(path = "", method = "GET")
  public void welcomeHandler(RoutingContext ctx) {
    new AppResponse().success("Welcome! The application is up and running.").send(ctx);
  }
}
