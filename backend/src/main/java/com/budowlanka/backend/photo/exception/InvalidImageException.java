package com.budowlanka.backend.photo.exception;

import java.io.Serial;

public class InvalidImageException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidImageException(String message) {
    super(message);
  }
}
