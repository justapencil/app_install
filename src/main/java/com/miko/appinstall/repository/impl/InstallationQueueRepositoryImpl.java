package com.miko.appinstall.repository.impl;

import com.miko.appinstall.model.entity.InstallationQueueEntity;
import com.miko.appinstall.repository.InstallationQueueRepository;
import org.hibernate.SessionFactory;

public class InstallationQueueRepositoryImpl extends VertxRepositoryImpl<InstallationQueueEntity, Long> implements InstallationQueueRepository {

  public InstallationQueueRepositoryImpl(SessionFactory sessionFactory) {
    super(sessionFactory, InstallationQueueEntity.class);
  }

}
