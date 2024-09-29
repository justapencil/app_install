package com.miko.appinstall.verticle;

import com.influxdb.client.InfluxDBClient;
import com.miko.appinstall.annotation.scanner.AnnotationRouteScanner;
import com.miko.appinstall.codec.InstallationQueueEntityCodec;
import com.miko.appinstall.config.AppConfig;
import com.miko.appinstall.config.InfluxDBConfig;
import com.miko.appinstall.config.MySQLConfig;
import com.miko.appinstall.controller.ApplicationController;
import com.miko.appinstall.controller.WelcomeController;
import com.miko.appinstall.handler.ApplicationHandler;
import com.miko.appinstall.handler.EmailHandler;
import com.miko.appinstall.handler.GlobalErrorHandler;
import com.miko.appinstall.handler.InstallationQueueHandler;
import com.miko.appinstall.listener.InstallationQueueEntityListener;
import com.miko.appinstall.repository.ApplicationRepository;
import com.miko.appinstall.repository.InstallationQueueRepository;
import com.miko.appinstall.repository.impl.ApplicationRepositoryImpl;
import com.miko.appinstall.repository.impl.InstallationQueueRepositoryImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  private SessionFactory sessionFactory;
  private InfluxDBClient influxDBClient;

  @Override
  public void start(Promise<Void> startPromise) {

    AppConfig appConfig = new AppConfig();
    int httpPort = appConfig.getHttpPort();

    InfluxDBConfig influxDBConfig = new InfluxDBConfig(appConfig.getInfluxDBConfig());
    influxDBClient = influxDBConfig.getInfluxDBClient();
    InstallationQueueEntityListener installationQueueEntityListener = new InstallationQueueEntityListener(influxDBClient);

    MySQLConfig mySQLConfig = new MySQLConfig(appConfig.getMySQLConfig(), appConfig.getHibernateConfig());
    sessionFactory = mySQLConfig.getSessionFactory();

    ApplicationRepository applicationRepository = new ApplicationRepositoryImpl(sessionFactory);
    InstallationQueueRepository installationQueueRepository = new InstallationQueueRepositoryImpl(sessionFactory,installationQueueEntityListener);

    InstallationQueueHandler installationQueueHandler = new InstallationQueueHandler(installationQueueRepository,vertx);
    ApplicationHandler applicationHandler = new ApplicationHandler(applicationRepository, installationQueueHandler);

    ApplicationController applicationController = new ApplicationController(applicationHandler);
    WelcomeController welcomeController = new WelcomeController();

    Router router = Router.router(vertx);

    router
      .route()
      .handler(BodyHandler.create())
      .handler(this::setDefaultContentType)
      .failureHandler(GlobalErrorHandler::handleFailure);
    Map<Class<?>, Object> controllerInstances = new HashMap<>();
    controllerInstances.put(ApplicationController.class, applicationController);
    controllerInstances.put(WelcomeController.class, welcomeController);
    AnnotationRouteScanner routeScanner = new AnnotationRouteScanner(router,controllerInstances);
    routeScanner.scanAndRegisterRoutes("com.miko.appinstall.controller");

    EmailHandler emailHandler = new EmailHandler(appConfig.getSmptConfig());

    vertx.eventBus().registerCodec(new InstallationQueueEntityCodec());
    DeploymentOptions options = new DeploymentOptions().setWorker(true);

    vertx.deployVerticle(new InstallationWorkerVerticle(installationQueueRepository,emailHandler), options, res -> {
      if (res.succeeded()) {
        log.info("InstallationWorkerVerticle deployed successfully.");
      } else {
        log.error("Failed to deploy InstallationWorkerVerticle: " + res.cause());
      }
    });

    vertx.createHttpServer().requestHandler(router).listen(httpPort, httpResult -> {
      if (httpResult.succeeded()) {
        log.info("HTTP server started on port " + httpPort);
        startPromise.complete();
      } else {
        startPromise.fail(httpResult.cause());
      }
    });
  }

  private void setDefaultContentType(RoutingContext ctx) {
    ctx.response().putHeader("Content-Type", "text/plain");
    ctx.next();
  }

  @Override
  public void stop() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
    if (influxDBClient != null) {
      influxDBClient.close();
    }
  }
}
