package com.budowlanka.backend.photo.exception;

import java.io.Serial;

public class PhotoNotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public PhotoNotFoundException() {
    super("Zdjęcie nie zostało znalezione.");
  }
}
