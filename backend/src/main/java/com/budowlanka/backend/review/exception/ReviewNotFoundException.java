package com.budowlanka.backend.review.exception;

import java.io.Serial;

public class ReviewNotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public ReviewNotFoundException() {
    super("Opinia nie została znaleziona.");
  }
}
