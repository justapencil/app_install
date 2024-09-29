package com.miko.appinstall.repository.impl;

import com.miko.appinstall.model.entity.ApplicationEntity;
import com.miko.appinstall.repository.ApplicationRepository;
import io.vertx.core.Future;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class ApplicationRepositoryImpl extends VertxRepositoryImpl<ApplicationEntity, Long> implements ApplicationRepository {

  public ApplicationRepositoryImpl(SessionFactory sessionFactory) {
    super(sessionFactory, ApplicationEntity.class);
  }

  @Override
  public Future<ApplicationEntity> findByAppName(String appName) {
    Future<ApplicationEntity> future = Future.future(promise -> {
      try (Session session = sessionFactory.openSession()) {
        ApplicationEntity result = session
          .createQuery("FROM ApplicationEntity WHERE appName = :appName", ApplicationEntity.class)
          .setParameter("appName", appName)
          .uniqueResult();

        if (result != null) {
          promise.complete(result);
        } else {
          promise.complete(null);
        }
      } catch (Exception e) {
        promise.fail(e);
      }
    });

    return future;
  }
}
