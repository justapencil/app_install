package com.miko.appinstall.config;

import com.miko.appinstall.model.entity.ApplicationEntity;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Map;

public class MySQLConfig {

  private final SessionFactory sessionFactory;

  public MySQLConfig(Map<String, Object> mysqlConfig, Map<String, Object> hibernateConfigYaml) {
    // Hibernate Configuration
    Configuration hibernateConfig = new Configuration();

    // Set Hibernate properties dynamically from YAML
    hibernateConfig.setProperty("hibernate.dialect", (String) hibernateConfigYaml.get("dialect"));
    hibernateConfig.setProperty("hibernate.connection.url", (String) mysqlConfig.get("url"));
    hibernateConfig.setProperty("hibernate.connection.username", (String) mysqlConfig.get("user"));
    hibernateConfig.setProperty("hibernate.connection.password", (String) mysqlConfig.get("password"));
    hibernateConfig.setProperty("hibernate.hbm2ddl.auto", (String) hibernateConfigYaml.get("hbm2ddl_auto"));
    hibernateConfig.setProperty("hibernate.show_sql", String.valueOf(hibernateConfigYaml.get("show_sql")));

    hibernateConfig.addAnnotatedClass(ApplicationEntity.class);
    hibernateConfig.addAnnotatedClass(InstallationQueueEntity.class);

    this.sessionFactory = hibernateConfig.buildSessionFactory();
  }
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}
