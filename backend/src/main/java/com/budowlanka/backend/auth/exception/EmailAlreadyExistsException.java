package com.budowlanka.backend.auth.exception;

import java.io.Serial;

public class EmailAlreadyExistsException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public EmailAlreadyExistsException() {
    super("Email jest już zajęty.");
  }
}
