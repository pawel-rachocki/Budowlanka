package com.budowlanka.backend.photo.exception;

import java.io.Serial;

public class PhotoOwnershipException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public PhotoOwnershipException() {
    super("Brak uprawnień do usunięcia tego zdjęcia.");
  }
}
