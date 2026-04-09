package com.budowlanka.backend.auth.exception;

import java.io.Serial;

public class AdminRegistrationException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public AdminRegistrationException() {
    super("Rola ADMIN nie może być wybrana podczas rejestracji.");
  }
}
