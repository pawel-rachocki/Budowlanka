package com.budowlanka.backend.admin.exception;

import java.io.Serial;

public class PhotoAlreadyDecidedException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public PhotoAlreadyDecidedException() {
    super("Zdjęcie zostało już zmoderowane.");
  }
}
