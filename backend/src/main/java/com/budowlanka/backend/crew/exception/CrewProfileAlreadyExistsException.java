package com.budowlanka.backend.crew.exception;

import java.io.Serial;

public class CrewProfileAlreadyExistsException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public CrewProfileAlreadyExistsException() {
    super("Użytkownik posiada już profil ekipy.");
  }
}
