package com.budowlanka.backend.review.exception;

import java.io.Serial;

public class ReviewOwnershipException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public ReviewOwnershipException() {
    super("Brak uprawnień do modyfikacji tej opinii.");
  }
}
