package com.miko.appinstall.repository;

import com.miko.appinstall.model.entity.ApplicationEntity;
import io.vertx.core.Future;

public interface ApplicationRepository extends VertxRepository<ApplicationEntity, Long>{

  Future<ApplicationEntity> findByAppName(String appName);
}
