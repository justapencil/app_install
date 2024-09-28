package com.miko.appinstall;

import com.miko.appinstall.config.AppConfig;
import com.miko.appinstall.config.InfluxDBConfig;
import com.miko.appinstall.config.MySQLConfig;
import com.miko.appinstall.controller.WelcomeController;
import com.miko.appinstall.handler.GlobalErrorHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hibernate.SessionFactory;
import org.influxdb.InfluxDB;


public class MainVerticle extends AbstractVerticle {

  private SessionFactory sessionFactory;
  private InfluxDB influxDBClient;

  @Override
  public void start(Promise<Void> startPromise) {

    AppConfig appConfig = new AppConfig();
    int httpPort = appConfig.getHttpPort();

    MySQLConfig mySQLConfig = new MySQLConfig(appConfig.getMySQLConfig(), appConfig.getHibernateConfig());
    sessionFactory = mySQLConfig.getSessionFactory();

    InfluxDBConfig influxDBConfig = new InfluxDBConfig(appConfig.getInfluxDBConfig());
    influxDBClient = influxDBConfig.getInfluxDBClient();

    Router router = Router.router(vertx);

    router
      .route()
      .handler(this::setDefaultContentType)
      .failureHandler(GlobalErrorHandler::handleFailure);

    new WelcomeController(router);

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
