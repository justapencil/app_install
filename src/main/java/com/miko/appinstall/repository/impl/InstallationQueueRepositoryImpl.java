package com.miko.appinstall.repository.impl;

import com.miko.appinstall.listener.InstallationQueueEntityListener;
import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class InstallationQueueRepositoryImpl extends VertxRepositoryImpl<InstallationQueueEntity, Long> implements InstallationQueueRepository {

  private final InstallationQueueEntityListener entityListener;

  public InstallationQueueRepositoryImpl(SessionFactory sessionFactory, InstallationQueueEntityListener entityListener) {
    super(sessionFactory, InstallationQueueEntity.class);
    this.entityListener = entityListener;
  }

  @Override
  public Future<InstallationQueueEntity> save(InstallationQueueEntity entity) {
    Promise<InstallationQueueEntity> promise = Promise.promise();
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session.saveOrUpdate(entity);
      session.getTransaction().commit();
      promise.complete(entity);
      entityListener.logInstallationEvent(entity);
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
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

  @Override
  public Future<List<InstallationQueueEntity>> findAllErrorTasks() {
    Promise<List<InstallationQueueEntity>> promise = Promise.promise();
    try (Session session = sessionFactory.openSession()) {
      List<InstallationQueueEntity> resultList = session
        .createQuery("FROM InstallationQueueEntity WHERE eventStatus = :status", InstallationQueueEntity.class)
        .setParameter("status", "ERROR")
        .list();
      promise.complete(resultList);
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }
}
