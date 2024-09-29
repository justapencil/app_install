package com.miko.appinstall.repository;

import com.miko.appinstall.model.entity.InstallationQueueEntity;
import io.vertx.core.Future;

import java.util.List;

public interface InstallationQueueRepository extends VertxRepository<InstallationQueueEntity, Long> {
  Future<InstallationQueueEntity> findByAppId(InstallationQueueEntity installationQueueEntity);

  Future<List<InstallationQueueEntity>> findAllErrorTasks();
}
