package com.miko.appinstall.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class AppConfig {

  private final Map<String, Object> config;

  public AppConfig() {
    Yaml yaml = new Yaml();
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.yml")) {
      if (inputStream == null) {
        throw new RuntimeException("application.yml not found in resources");
      }
      config = yaml.load(inputStream);
    } catch (Exception e) {
      throw new RuntimeException("Unable to load application.yml", e);
    }
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public int getHttpPort() {
    Map<String, Object> httpConfig = (Map<String, Object>) config.get("http");
    return (int) httpConfig.getOrDefault("port", 8080);
  }

  public Map<String, Object> getMySQLConfig() {
    return (Map<String, Object>) config.get("mysql");
  }

  public Map<String, Object> getHibernateConfig() {
    return (Map<String, Object>) config.get("hibernate");
  }

  public Map<String, Object> getInfluxDBConfig() {
    return (Map<String, Object>) config.get("influxdb");
  }

  public Map<String, Object> getSmptConfig() {
    return (Map<String, Object>) config.get("smpt");
  }
}
