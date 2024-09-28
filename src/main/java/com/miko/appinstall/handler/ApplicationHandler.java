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
  private final InstallationHandler installationHandler;

  public void fetchAllApps(RoutingContext routingContext) {
    applicationRepository.findAll()
      .onSuccess(applications -> {
        new AppResponse()
          .json(applications)
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
      .compose(v -> {
        ApplicationEntity applicationEntity = this.addApp(applicationRequest).result();
        InstallationQueueEntity installationQueueEntity = new InstallationQueueEntity(applicationEntity.getId());
        return installationHandler.addToInstallationQueue(installationQueueEntity)
          .compose(queueSuccess -> applicationRepository.save(applicationEntity));
      })
      .onSuccess(savedApp -> {
        new AppResponse<>()
          .json(savedApp)
          .status(201)
          .send(routingContext);
      })
      .onFailure(failure -> {
        new AppResponse<>()
          .status(400)
          .json("Failed in adding application to installation queue: " + failure.getMessage())
          .send(routingContext);
      });
  }
}
