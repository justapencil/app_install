package com.miko.appinstall.model.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "app_core")
public class ApplicationEntity extends AuditModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "app_name", nullable = false)
  private String appName;

  @Column(name = "version", nullable = false)
  private String version;

}
