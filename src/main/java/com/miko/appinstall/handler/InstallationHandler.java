package com.miko.appinstall.handler;

import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class InstallationHandler {

  private final InstallationQueueRepository installationQueueRepository;

  public Future<Void> addToInstallationQueue(InstallationQueueEntity installationQueueEntity) {
    return installationQueueRepository.findByAppId(installationQueueEntity)
      .onSuccess(installationQueueEntity1 -> {
        if (installationQueueEntity1 != null) {
          log.info("Installation already exists for app id {}", installationQueueEntity.getAppId());
          return;
        } else {
          installationQueueRepository.save(installationQueueEntity);
        }
        log.info("Added installation to app id {} to queue", installationQueueEntity.getAppId());
      })
      .onFailure(throwable -> {
        log.error("Failed to add installation, app id: {}", installationQueueEntity.getAppId(), throwable);
      }).mapEmpty();
  }
}
