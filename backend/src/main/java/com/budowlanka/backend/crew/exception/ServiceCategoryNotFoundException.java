package com.budowlanka.backend.crew.exception;

import java.io.Serial;

public class ServiceCategoryNotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public ServiceCategoryNotFoundException() {
    super("Jedna lub więcej kategorii usług nie istnieje.");
  }
}
