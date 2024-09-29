package com.miko.appinstall.verticle;

import com.miko.appinstall.constant.enums.EventStatusEnum;
import com.miko.appinstall.constant.enums.EventTypeEnum;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;


@Slf4j
public class InstallationWorkerVerticle extends AbstractVerticle {

  private final InstallationQueueRepository installationQueueRepository;

  public InstallationWorkerVerticle(InstallationQueueRepository installationQueueRepository) {
    this.installationQueueRepository = installationQueueRepository;
  }

  @Override
  public void start() {
    vertx.eventBus().consumer("installation.process", message -> {
      InstallationQueueEntity task = (InstallationQueueEntity) message.body();
      updateInstallationStatus(task, EventStatusEnum.PICKEDUP).onComplete(res -> {
        log.info("Installation picked up for app id: {}", task.getAppId());
      });
      log.info("Processing installation for app id: {}", task.getAppId());
      processInstallation(task)
        .compose(result -> updateInstallationStatus(task, EventStatusEnum.COMPLETED))
        .onSuccess(success -> {
          log.info("Installation completed successfully for app id: {}", task.getAppId());
          message.reply("Installation completed successfully");
        })
        .onFailure(err -> {
          handleFailureAndRetry(task, err.getMessage());
        });
    });
  }

  private Future<Void> processInstallation(InstallationQueueEntity task) {
    Promise<Void> promise = Promise.promise();

    vertx.setTimer(2000, id -> {
      boolean success = new Random().nextBoolean();
      if (success) {
        promise.complete();
      } else {
        promise.fail("Installation failed due to XYZ reason");
      }
    });

    return promise.future();
  }

  private Future<Void> updateInstallationStatus(InstallationQueueEntity task, EventStatusEnum status) {
    task.setEventStatus(status);
    if (status.equals(EventStatusEnum.COMPLETED)) {
      task.setVersion(task.getVersion() == null ? "v1.0" : task.getVersion());
    }
    return installationQueueRepository.save(task).mapEmpty();
  }

  private void handleFailureAndRetry(InstallationQueueEntity task, String reason) {
    log.error("Installation failed for app id: {}. Reason: {}", task.getAppId(), reason);
    task.setRetryAttempt(task.getRetryAttempt() + 1);
    task.setRetryReason(reason);

    if (task.getRetryAttempt() < 3) {
      updateInstallationStatus(task, EventStatusEnum.SCHEDULED).onComplete(result -> {
        long delay = 5000L * task.getRetryAttempt();
        vertx.setTimer(delay, id -> {
          DeliveryOptions options = new DeliveryOptions().setCodecName("InstallationQueueEntityCodec");
          vertx.eventBus().send("installation.process", task, options);
        });
      });
    } else {
      task.setEventStatus(EventStatusEnum.ERROR);
      updateInstallationStatus(task, EventStatusEnum.ERROR).onComplete(res -> {
        log.info("Task permanently failed after retries.");
      });
    }
  }
}
