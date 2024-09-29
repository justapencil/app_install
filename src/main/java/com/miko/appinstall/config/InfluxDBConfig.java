package com.miko.appinstall.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
public class InfluxDBConfig {

  private static InfluxDBClient influxDBClient;

  public InfluxDBConfig(String influxDBUrl, String influxDBToken, String influxDBOrg, String influxDBBucket) {
    if (influxDBClient == null) {
      synchronized (InfluxDBConfig.class) {
        if (influxDBClient == null) {
          influxDBClient = InfluxDBClientFactory.create(influxDBUrl, influxDBToken.toCharArray(), influxDBOrg, influxDBBucket);
          log.info("InfluxDBClient initialized with URL: {}", influxDBUrl);

          // Test the connection by pinging InfluxDB
          try {
            if (influxDBClient.ping().booleanValue()) {
              log.info("Successfully connected to InfluxDB.");
            } else {
              log.warn("Failed to connect to InfluxDB.");
            }
          } catch (Exception e) {
            log.error("Error connecting to InfluxDB: {}", e.getMessage());
          }
        }
      }
    }
  }
  public InfluxDBConfig(Map<String, Object> influxDBConfig) {
    this(
      (String) influxDBConfig.get("url"),
      (String) influxDBConfig.get("token"),
      (String) influxDBConfig.get("org"),
      (String) influxDBConfig.get("bucket")
    );
  }

  public InfluxDBClient getInfluxDBClient() {
    return influxDBClient;
  }
}
