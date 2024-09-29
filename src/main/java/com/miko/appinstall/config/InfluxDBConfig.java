package com.miko.appinstall.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class InfluxDBConfig {

  private static InfluxDBClient influxDBClient;

  // Constructor using explicit parameters for type-safety
  public InfluxDBConfig(String influxDBUrl, String influxDBToken, String influxDBOrg, String influxDBBucket) {
    if (influxDBClient == null) {
      influxDBClient = InfluxDBClientFactory.create(influxDBUrl, influxDBToken.toCharArray(), influxDBOrg, influxDBBucket);
      log.info("InfluxDBClient initialized with URL: {}", influxDBUrl);
    }
  }

  // Overloaded constructor that accepts Map, for backward compatibility
  public InfluxDBConfig(Map<String, Object> influxDBConfig) {
    this(
      (String) influxDBConfig.get("url"),
      (String) influxDBConfig.get("token"),
      (String) influxDBConfig.get("org"),
      (String) influxDBConfig.get("bucket")
    );
  }

  // Singleton method to retrieve the client instance
  public static InfluxDBClient getInfluxDBClientInstance() {
    if (influxDBClient == null) {
      throw new IllegalStateException("InfluxDBClient has not been initialized. Call the constructor first.");
    }
    return influxDBClient;
  }

  // Method to write data using the Point class (recommended)
  public void writeData(String measurement, Map<String, Object> fields, String tagKey, String tagValue) {
    Point point = Point
      .measurement(measurement)
      .addTag(tagKey, tagValue)
      .time(Instant.now(), WritePrecision.NS);

    // Add fields to the Point object
    fields.forEach((key, value) -> {
      if (value instanceof String) {
        point.addField(key, (String) value);
      } else if (value instanceof Integer) {
        point.addField(key, (Integer) value);
      } else if (value instanceof Double) {
        point.addField(key, (Double) value);
      } else if (value instanceof Boolean) {
        point.addField(key, (Boolean) value);
      } else if (value instanceof Long) {
        point.addField(key, (Long) value);
      }
    });

    // Use the WriteApi to write the point to InfluxDB
    try (WriteApi writeApi = influxDBClient.getWriteApi()) {
      writeApi.writePoint(point);
      log.info("Data written to InfluxDB: {}", point.toLineProtocol());
    } catch (Exception e) {
      log.error("Error writing data to InfluxDB: {}", e.getMessage(), e);
    }
  }

  // Optional getter for the InfluxDBClient if needed
  public InfluxDBClient getInfluxDBClient() {
    return influxDBClient;
  }
}
