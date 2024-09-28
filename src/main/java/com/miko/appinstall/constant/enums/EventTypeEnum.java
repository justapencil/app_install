package com.miko.appinstall.constant.enums;

public enum EventTypeEnum {
  ADD, // The app is queued for installation.
  UPDATE, // The app has been picked up for downloading and installation.
  RETRY, // An error occurred during installation. The system will retry the installation up to three times.

}
