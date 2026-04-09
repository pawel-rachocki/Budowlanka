package com.budowlanka.backend.auth.exception;

import java.io.Serial;

public class VerificationTokenException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public VerificationTokenException(String message) {
    super(message);
  }
}
