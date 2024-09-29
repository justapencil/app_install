package com.miko.appinstall.model;

import io.vertx.core.Future;
import lombok.Getter;

@Getter
public class ApplicationRequest {
  private String name;
  private String version;

  public Future<Object> validate() {
    if (name == null || name.isBlank()) {
      return Future.failedFuture("Name is required");
    }
    return Future.succeededFuture();
  }
}
