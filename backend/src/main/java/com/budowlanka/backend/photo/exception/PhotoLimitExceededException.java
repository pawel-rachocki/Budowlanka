package com.budowlanka.backend.photo.exception;

import java.io.Serial;

public class PhotoLimitExceededException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public PhotoLimitExceededException() {
    super("Osiągnięto limit 20 zdjęć w portfolio.");
  }
}
