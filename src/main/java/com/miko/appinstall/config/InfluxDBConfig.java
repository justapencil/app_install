package com.miko.appinstall.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import org.influxdb.InfluxDB;

import java.util.Map;

public class InfluxDBConfig {

  private static InfluxDBClient influxDBClient;

  public InfluxDBConfig(Map<String, Object> influxDBConfig) {
    String influxDBUrl = (String) influxDBConfig.get("url");
    String influxDBToken = (String) influxDBConfig.get("token");
    String influxDBOrg = (String) influxDBConfig.get("org");
    String influxDBBucket = (String) influxDBConfig.get("bucket");

    if (influxDBClient == null) {
      influxDBClient = InfluxDBClientFactory.create(influxDBUrl, influxDBToken.toCharArray(), influxDBOrg, influxDBBucket);
    }
  }

  public static InfluxDBClient getInfluxDBClientInstance() {
    return influxDBClient;
  }

  public void writeData(String measurement, Map<String, Object> fields, String tagKey, String tagValue) {
    try (WriteApi writeApi = influxDBClient.getWriteApi()) {
      String lineProtocol = String.format("%s,%s=%s %s", measurement, tagKey, tagValue, formatFields(fields));
      writeApi.writeRecord(WritePrecision.NS, lineProtocol);
    }
  }


  private String formatFields(Map<String, Object> fields) {
    StringBuilder fieldString = new StringBuilder();
    fields.forEach((key, value) -> {
      if (value instanceof String) {
        fieldString.append(key).append("=\"").append(value).append("\",");
      } else {
        fieldString.append(key).append("=").append(value).append(",");
      }
    });
    return fieldString.substring(0, fieldString.length() - 1);
  }

  public InfluxDBClient getInfluxDBClient() {
    return influxDBClient;
  }
}
