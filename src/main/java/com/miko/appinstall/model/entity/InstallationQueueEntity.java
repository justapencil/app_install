package com.miko.appinstall.model.entity;

import com.miko.appinstall.constant.enums.EventStatusEnum;
import com.miko.appinstall.constant.enums.EventTypeEnum;
import com.miko.appinstall.listener.InstallationQueueEntityListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "installation_queue")
public class InstallationQueueEntity extends AuditModel  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "app_id", nullable = false)
  private Long appId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private EventTypeEnum eventType = EventTypeEnum.ADD;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_status", nullable = false)
  private EventStatusEnum eventStatus = EventStatusEnum.SCHEDULED;

  @Column(name = "version")
  private String version;

  @Column(name = "retry_attempt")
  private Integer retryAttempt = 0;

  @Column(name = "retry_reason")
  private String retryReason;

  public InstallationQueueEntity(Long id,String version) {
    this.appId = id;
    this.version = version;
  }
}
