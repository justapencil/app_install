package com.miko.appinstall.handler;

import com.miko.appinstall.model.ApplicationRequest;
import com.miko.appinstall.model.entity.ApplicationEntity;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.model.response.AppResponse;
import com.miko.appinstall.repository.ApplicationRepository;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApplicationHandler {

  private final ApplicationRepository applicationRepository;
  private final InstallationQueueHandler installationQueueHandler;

  public void fetchAllApps(RoutingContext routingContext) {
    applicationRepository.findAll()
      .onSuccess(applications -> {
        new AppResponse<>()
          .json(applications)
          .send(routingContext);
      })
      .onFailure(failure -> {
        new AppResponse<>()
          .status(500)
          .json("Failed to fetch applications: " + failure.getMessage())
          .send(routingContext);
      });
  }

  /**
   * Check if the application already exists in the database, if not add it.
   *
   * @param applicationRequest the application request data to process
   * @return Future<ApplicationEntity> the existing or newly created application
   */
  private Future<ApplicationEntity> addApp(ApplicationRequest applicationRequest) {
    return applicationRepository.findByAppName(applicationRequest.getName())
      .compose(existingApp -> {
        if (existingApp != null) {
          return Future.succeededFuture(existingApp);
        } else {
          ApplicationEntity newApplication = new ApplicationEntity(applicationRequest);
          return applicationRepository.save(newApplication);
        }
      });
  }

  public void installApp(RoutingContext routingContext) {

    ApplicationRequest applicationRequest = routingContext.getBodyAsJson().mapTo(ApplicationRequest.class);

    applicationRequest.validate()
      .compose(v -> addApp(applicationRequest))
      .compose(applicationEntity -> {
        InstallationQueueEntity installationQueueEntity = new InstallationQueueEntity(applicationEntity.getId(),applicationRequest.getVersion());
        return installationQueueHandler.addToInstallationQueue(installationQueueEntity)
          .compose(queueSuccess -> Future.succeededFuture(applicationEntity));
      })
      .onSuccess(savedApp -> {
        new AppResponse<>()
          .json(savedApp)
          .status(201)
          .send(routingContext);
      })
      .onFailure(failure -> {
        new AppResponse().badRequest("Failed in adding application to installation queue: " + failure.getMessage()).send(routingContext);
      });
  }
}
