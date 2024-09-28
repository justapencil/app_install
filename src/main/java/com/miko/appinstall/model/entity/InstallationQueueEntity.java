package com.miko.appinstall.model.entity;

import com.miko.appinstall.constant.enums.EventStatusEnum;
import com.miko.appinstall.constant.enums.EventTypeEnum;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "installation_queue")
public class InstallationQueueEntity extends AuditModel  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "app_id", nullable = false)
  private Long appId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private EventTypeEnum eventType;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_status", nullable = false)
  private EventStatusEnum eventStatus;

  @Column(name = "version", nullable = false)
  private String version;

  @Column(name = "retry_attempt")
  private Integer retryAttempt;

  @Column(name = "retry_reason")
  private String retryReason;

}
