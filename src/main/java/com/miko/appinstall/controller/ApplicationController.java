package com.miko.appinstall.controller;

import com.miko.appinstall.annotation.RouteMapping;
import com.miko.appinstall.annotation.RouteController;
import com.miko.appinstall.handler.ApplicationHandler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RouteController(path = "/applications")
public class ApplicationController {

  private final ApplicationHandler applicationHandler;  

  @RouteMapping(path = "/fetch-all", method = "GET")
  public void fetchAllApps(RoutingContext ctx) {
    applicationHandler.fetchAllApps(ctx);
  }

  @RouteMapping(path = "/add", method = "POST")
  public void installApp(RoutingContext ctx) {
    applicationHandler.installApp(ctx);
  }
}
