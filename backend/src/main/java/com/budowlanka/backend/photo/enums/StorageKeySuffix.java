package com.budowlanka.backend.photo.enums;

public enum StorageKeySuffix {
  ORIGINAL("original"),
  THUMB("thumb");

  private final String value;

  StorageKeySuffix(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
