package com.miko.appinstall.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.Map;

public class InfluxDBConfig {

  private final InfluxDB influxDBClient;

  public InfluxDBConfig(Map<String, Object> influxDBConfig) {
    String influxDBUrl = (String) influxDBConfig.get("url");
    String influxDBUsername = (String) influxDBConfig.get("username");
    String influxDBPassword = (String) influxDBConfig.get("password");

    this.influxDBClient = InfluxDBFactory.connect(influxDBUrl, influxDBUsername, influxDBPassword);
    this.influxDBClient.setDatabase((String) influxDBConfig.get("database"));
  }
  public InfluxDB getInfluxDBClient() {
    return influxDBClient;
  }
}
