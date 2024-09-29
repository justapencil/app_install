package com.miko.appinstall.verticle;

import com.miko.appinstall.constant.enums.EventStatusEnum;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

@Slf4j
public class InstallationWorkerVerticle extends AbstractVerticle {

  private static final int MAX_RETRY_ATTEMPTS = 3;
  private final InstallationQueueRepository installationQueueRepository;

  // Active tasks counter or Set for tracking active tasks
  private final AtomicInteger activeTasks = new AtomicInteger(0);  // Option 1: counter
  // private final Set<Long> activeTasksSet = ConcurrentHashMap.newKeySet();  // Option 2: Set for task IDs

  public InstallationWorkerVerticle(InstallationQueueRepository installationQueueRepository) {
    this.installationQueueRepository = installationQueueRepository;
  }

  @Override
  public void start() {
    vertx.eventBus().consumer("installation.process", message -> {
      InstallationQueueEntity task = (InstallationQueueEntity) message.body();
      activeTasks.incrementAndGet();
      log.info("Active tasks: {}", activeTasks.get());
      updateInstallationStatus(task, EventStatusEnum.PICKEDUP).onComplete(res -> {
        log.info("Installation picked up for app id: {}", task.getAppId());
      });

      log.info("Processing installation for app id: {}", task.getAppId());

      processInstallation(task)
        .compose(result -> updateInstallationStatus(task, EventStatusEnum.COMPLETED))  // Mark as completed on success
        .onSuccess(success -> {
          log.info("Installation completed successfully for app id: {}", task.getAppId());
          message.reply("Installation completed successfully");
          completeTaskProcessing(task);
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
      task.setDefaultVersion();  // Set default version if completed
    }
    return installationQueueRepository.save(task).mapEmpty();
  }

  private void handleFailureAndRetry(InstallationQueueEntity task, String reason) {
    log.error("Installation failed for app id: {}. Reason: {}", task.getAppId(), reason);

    task.setRetryAttempt(task.getRetryAttempt() + 1);  // Increment retry attempt
    task.setRetryReason(reason);

    if (task.getRetryAttempt() < MAX_RETRY_ATTEMPTS) {
      log.info("Retrying installation for app id: {}. Attempt: {}", task.getAppId(), task.getRetryAttempt());

      updateInstallationStatus(task, EventStatusEnum.SCHEDULED).onComplete(result -> {
        long delay = 5000L * task.getRetryAttempt();
        vertx.setTimer(delay, id -> {
          DeliveryOptions options = new DeliveryOptions().setCodecName("InstallationQueueEntityCodec");
          vertx.eventBus().send("installation.process", task, options);
        });
      });

    } else {
      // Mark the task as permanently failed
      log.info("Max retries reached for app id: {}. Marking as failed.", task.getAppId());
      task.setEventStatus(EventStatusEnum.ERROR);  // Mark as failed

      // Save the task with the failed status
      updateInstallationStatus(task, EventStatusEnum.ERROR).onComplete(res -> {
        log.info("Task permanently failed after {} retries for app id: {}", MAX_RETRY_ATTEMPTS, task.getAppId());

        // Decrement active task counter and check if all are done
        completeTaskProcessing(task);
      });
    }
  }

  private void completeTaskProcessing(InstallationQueueEntity task) {
    int remainingTasks = activeTasks.decrementAndGet();
    if (remainingTasks == 0) {
      log.info("All scheduled tasks have been processed. Rescheduling ERROR tasks.");
      rescheduleErrorTasks();
    }
  }

  /**
   * Reschedules tasks that are in ERROR state.
   */
  private void rescheduleErrorTasks() {
    installationQueueRepository.findAllErrorTasks().onSuccess(errorTasks -> {
      for (InstallationQueueEntity errorTask : errorTasks) {
        log.info("Rescheduling task with app id: {} from ERROR state.", errorTask.getAppId());
        errorTask.setEventStatus(EventStatusEnum.SCHEDULED);
        updateInstallationStatus(errorTask, EventStatusEnum.SCHEDULED).onComplete(result -> {
          DeliveryOptions options = new DeliveryOptions().setCodecName("InstallationQueueEntityCodec");
          vertx.eventBus().send("installation.process", errorTask, options);
        });
      }
    });
  }
}
