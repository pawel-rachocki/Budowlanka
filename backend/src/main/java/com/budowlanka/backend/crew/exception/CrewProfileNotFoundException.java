package com.budowlanka.backend.crew.exception;

import java.io.Serial;

public class CrewProfileNotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public CrewProfileNotFoundException() {
    super("Profil ekipy nie został znaleziony.");
  }
}
