package com.miko.appinstall.listener;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

@Slf4j
public class InstallationQueueEntityListener {

  private final InfluxDBClient influxDBClient;

  public InstallationQueueEntityListener(InfluxDBClient influxDBClient) {
    this.influxDBClient = influxDBClient;
  }

  @PrePersist
  @PreUpdate
  public void logInstallationEvent(InstallationQueueEntity entity) {
    writeToInfluxDB(entity);
  }

  private void writeToInfluxDB(InstallationQueueEntity entity) {
    log.info("Writing installation event to InfluxDB");
    String measurement = "installation_logs";

    Point point = Point
      .measurement(measurement)
      .addTag("appId", String.valueOf(entity.getAppId()))
      .addField("eventType", entity.getEventType().name())
      .addField("eventStatus", entity.getEventStatus().name())
      .addField("version", entity.getVersion())
      .addField("retryAttempt", entity.getRetryAttempt())
      .addField("retryReason", entity.getRetryReason() != null ? entity.getRetryReason() : "")
      .time(Instant.now(), WritePrecision.NS);

    // Use the InfluxDB client to write data
    try (WriteApi writeApi = influxDBClient.getWriteApi()) {
      writeApi.writePoint(point);
      log.info("Installation event written to InfluxDB");
    }
  }
}
