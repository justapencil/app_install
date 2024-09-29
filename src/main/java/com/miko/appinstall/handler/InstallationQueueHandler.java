package com.miko.appinstall.handler;

import com.miko.appinstall.constant.enums.EventStatusEnum;
import com.miko.appinstall.constant.enums.EventTypeEnum;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class InstallationQueueHandler {

  private final InstallationQueueRepository installationQueueRepository;
  private final Vertx vertx;

  /**
   * Adds a new installation to the queue or updates the existing one.
   *
   * @param installationQueueEntity the installation entity
   * @return a future representing the operation completion
   */
  public Future<Void> addToInstallationQueue(InstallationQueueEntity installationQueueEntity) {
    return installationQueueRepository.findByAppId(installationQueueEntity)
      .compose(existingEntity -> {
        if (existingEntity != null) {
          log.info("Installation already exists for app id {}", installationQueueEntity.getAppId());
          return handleExistingInstallation(existingEntity, installationQueueEntity);
        } else {
          installationQueueEntity.setDefaultVersion();
          return saveAndProcessInstallation(installationQueueEntity, EventTypeEnum.ADD, EventStatusEnum.SCHEDULED);
        }
      })
      .onFailure(throwable -> {
        log.error("Failed to add installation, app id: {}", installationQueueEntity.getAppId(), throwable);
      })
      .mapEmpty();
  }

  /**
   * Handles an existing installation entity by checking its status and applying logic accordingly.
   *
   * @param existingEntity          the existing installation entity
   * @param incomingEntity          the incoming installation entity
   * @return a future representing the operation completion
   */
  private Future<Void> handleExistingInstallation(InstallationQueueEntity existingEntity, InstallationQueueEntity incomingEntity) {
    switch (existingEntity.getEventStatus()) {
      case SCHEDULED:
      case PICKEDUP:
        return handleScheduledOrPickedUp(existingEntity, incomingEntity);

      case COMPLETED:
        if (incomingEntity.getVersion() != null && !incomingEntity.getVersion().equals(existingEntity.getVersion())) {
          existingEntity.setVersion(incomingEntity.getVersion());
          return saveAndProcessInstallation(existingEntity, EventTypeEnum.UPDATE, EventStatusEnum.SCHEDULED);
        }
        break;

      case ERROR:
        existingEntity.resetRetry();
        return saveAndProcessInstallation(existingEntity, EventTypeEnum.ADD, EventStatusEnum.SCHEDULED);

      default:
        log.info("Unhandled event status for app id {}", incomingEntity.getAppId());
    }
    return Future.succeededFuture();
  }

  /**
   * Handles cases where the installation is in SCHEDULED or PICKEDUP state.
   *
   * @param existingEntity  the existing installation entity
   * @param incomingEntity  the incoming installation entity
   * @return a future representing the operation completion
   */
  private Future<Void> handleScheduledOrPickedUp(InstallationQueueEntity existingEntity, InstallationQueueEntity incomingEntity) {
    if (incomingEntity.getVersion() != null) {
      if (existingEntity.getVersion() == null || !existingEntity.getVersion().equals(incomingEntity.getVersion())) {
        existingEntity.setVersion(incomingEntity.getVersion());
        return saveAndProcessInstallation(existingEntity, EventTypeEnum.UPDATE, EventStatusEnum.SCHEDULED);
      }
    }
    return Future.succeededFuture();
  }

  /**
   * Saves and processes an installation by updating the event type and status, and sending it to the event bus.
   *
   * @param entity       the installation entity
   * @param eventType    the event type to set
   * @param eventStatus  the event status to set
   * @return a future representing the save operation
   */
  private Future<Void> saveAndProcessInstallation(InstallationQueueEntity entity, EventTypeEnum eventType, EventStatusEnum eventStatus) {
    entity.setEventType(eventType);
    entity.setEventStatus(eventStatus);

    return installationQueueRepository.save(entity)
      .compose(savedEntity -> {
        DeliveryOptions options = new DeliveryOptions().setCodecName("InstallationQueueEntityCodec");
        vertx.eventBus().send("installation.process", savedEntity, options);

        log.info("Installation for app id {} added/updated and sent to the event bus", entity.getAppId());
        return Future.succeededFuture();
      });
  }
}
