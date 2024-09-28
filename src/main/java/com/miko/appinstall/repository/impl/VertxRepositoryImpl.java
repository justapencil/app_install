package com.miko.appinstall.repository.impl;

import com.miko.appinstall.repository.VertxRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
public class VertxRepositoryImpl<T, ID extends Serializable> implements VertxRepository<T, ID> {

  private final SessionFactory sessionFactory;
  private final Class<T> entityType;

  @Override
  public Future<T> findById(ID id) {
    Promise<T> promise = Promise.promise();
    try (Session session = sessionFactory.openSession()) {
      T entity = session.get(entityType, id);
      promise.complete(entity);
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }

  @Override
  public Future<List<T>> findAll() {
    Promise<List<T>> promise = Promise.promise();
    try (Session session = sessionFactory.openSession()) {
      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<T> query = builder.createQuery(entityType);
      Root<T> root = query.from(entityType);
      query.select(root);
      List<T> results = session.createQuery(query).getResultList();
      promise.complete(results);
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }

  @Override
  public Future<T> save(T entity) {
    Promise<T> promise = Promise.promise();
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session.saveOrUpdate(entity);
      session.getTransaction().commit();
      promise.complete(entity);
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }

  @Override
  public Future<Void> deleteById(ID id) {
    Promise<Void> promise = Promise.promise();
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      T entity = session.get(entityType, id);
      if (entity != null) {
        session.delete(entity);
      }
      session.getTransaction().commit();
      promise.complete();
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }
}
