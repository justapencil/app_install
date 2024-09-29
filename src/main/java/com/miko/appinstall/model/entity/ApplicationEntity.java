package com.miko.appinstall.model.entity;

import com.miko.appinstall.model.ApplicationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "app_core")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationEntity extends AuditModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "app_name", nullable = false)
  private String appName;

  public ApplicationEntity(ApplicationRequest applicationRequest) {
    this.appName = applicationRequest.getName();
  }
}
