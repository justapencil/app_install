package com.miko.appinstall.repository;

import io.vertx.core.Future;
import java.io.Serializable;
import java.util.List;

public interface VertxRepository<T, ID extends Serializable> {

  Future<T> findById(ID id);

  Future<List<T>> findAll();

  Future<T> save(T entity);

  Future<Void> deleteById(ID id);
}
