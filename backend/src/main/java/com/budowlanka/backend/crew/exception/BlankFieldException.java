package com.budowlanka.backend.crew.exception;

import java.io.Serial;

public class BlankFieldException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public BlankFieldException(String fieldName) {
    super("Pole " + fieldName + " nie może być puste.");
  }
}
