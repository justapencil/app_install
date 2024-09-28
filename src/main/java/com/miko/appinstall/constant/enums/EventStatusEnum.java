package com.miko.appinstall.constant.enums;

public enum EventStatusEnum {
  SCHEDULED, // The app is queued for installation.
  PICKEDUP, // The app has been picked up for downloading and installation.
  ERROR, // An error occurred during installation. The system will retry the installation up to three times.
  COMPLETED // The app installation has been successfully completed.
}
