package com.budowlanka.backend.review.exception;

import java.io.Serial;

public class DuplicateReviewException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public DuplicateReviewException() {
    super("Już wystawiłeś opinię tej ekipie.");
  }
}
