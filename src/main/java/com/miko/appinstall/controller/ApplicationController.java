package com.miko.appinstall.controller;

import com.miko.appinstall.handler.ApplicationHandler;
import io.vertx.ext.web.Router;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApplicationController {

  private final ApplicationHandler applicationHandler;

  public void initRoutes(Router router) {
    router.get("/applications/fetch-all").handler(applicationHandler::fetchAllApps);
    router.post("/applications/add").handler(applicationHandler::installApp);
  }


}
