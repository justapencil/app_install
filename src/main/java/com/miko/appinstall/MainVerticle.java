package com.miko.appinstall;

import com.influxdb.client.InfluxDBClient;
import com.miko.appinstall.annotation.scanner.AnnotationRouteScanner;
import com.miko.appinstall.config.AppConfig;
import com.miko.appinstall.config.InfluxDBConfig;
import com.miko.appinstall.config.MySQLConfig;
import com.miko.appinstall.controller.ApplicationController;
import com.miko.appinstall.controller.WelcomeController;
import com.miko.appinstall.handler.ApplicationHandler;
import com.miko.appinstall.handler.GlobalErrorHandler;
import com.miko.appinstall.handler.InstallationHandler;
import com.miko.appinstall.listener.InstallationQueueEntityListener;
import com.miko.appinstall.repository.ApplicationRepository;
import com.miko.appinstall.repository.InstallationQueueRepository;
import com.miko.appinstall.repository.impl.ApplicationRepositoryImpl;
import com.miko.appinstall.repository.impl.InstallationQueueRepositoryImpl;
import com.miko.appinstall.repository.impl.VertxRepositoryImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.hibernate.SessionFactory;
import org.influxdb.InfluxDB;

import java.util.HashMap;
import java.util.Map;


public class MainVerticle extends AbstractVerticle {

  private SessionFactory sessionFactory;
  private InfluxDBClient influxDBClient;

  @Override
  public void start(Promise<Void> startPromise) {

    AppConfig appConfig = new AppConfig();
    int httpPort = appConfig.getHttpPort();

    MySQLConfig mySQLConfig = new MySQLConfig(appConfig.getMySQLConfig(), appConfig.getHibernateConfig());
    sessionFactory = mySQLConfig.getSessionFactory();

    ApplicationRepository applicationRepository = new ApplicationRepositoryImpl(sessionFactory);
    InstallationQueueRepository installationQueueRepository = new InstallationQueueRepositoryImpl(sessionFactory);

    InstallationHandler installationHandler = new InstallationHandler(installationQueueRepository);
    ApplicationHandler applicationHandler = new ApplicationHandler(applicationRepository, installationHandler);

    ApplicationController applicationController = new ApplicationController(applicationHandler);
    WelcomeController welcomeController = new WelcomeController();
    InfluxDBConfig influxDBConfig = new InfluxDBConfig(appConfig.getInfluxDBConfig());
    influxDBClient = influxDBConfig.getInfluxDBClient();
    InstallationQueueEntityListener installationQueueEntityListener = new InstallationQueueEntityListener(influxDBClient);
    Router router = Router.router(vertx);

    router
      .route()
      .handler(BodyHandler.create()) // Add BodyHandler first to handle request bodies
      .handler(this::setDefaultContentType) // Then handle setting content type
      .failureHandler(GlobalErrorHandler::handleFailure);
    Map<Class<?>, Object> controllerInstances = new HashMap<>();
    controllerInstances.put(ApplicationController.class, applicationController);
    controllerInstances.put(WelcomeController.class, welcomeController);
    AnnotationRouteScanner routeScanner = new AnnotationRouteScanner(router,controllerInstances);
    routeScanner.scanAndRegisterRoutes("com.miko.appinstall.controller");

    vertx.createHttpServer().requestHandler(router).listen(httpPort, httpResult -> {
      if (httpResult.succeeded()) {
        System.out.println("HTTP server started on port " + httpPort);
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
