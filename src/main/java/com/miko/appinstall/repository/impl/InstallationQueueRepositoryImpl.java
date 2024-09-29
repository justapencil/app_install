package com.miko.appinstall.repository.impl;

import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import io.vertx.core.Future;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class InstallationQueueRepositoryImpl extends VertxRepositoryImpl<InstallationQueueEntity, Long> implements InstallationQueueRepository {

  public InstallationQueueRepositoryImpl(SessionFactory sessionFactory) {
    super(sessionFactory, InstallationQueueEntity.class);
  }

  @Override
  public Future<InstallationQueueEntity> findByAppId(InstallationQueueEntity installationQueueEntity) {
    Future<InstallationQueueEntity> future = Future.future(promise -> {
      try (Session session = sessionFactory.openSession()) {
        InstallationQueueEntity result = session
          .createQuery("FROM InstallationQueueEntity WHERE appId = :appId", InstallationQueueEntity.class)
          .setParameter("appId", installationQueueEntity.getAppId())
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
