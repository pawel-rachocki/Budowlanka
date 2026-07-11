package com.budowlanka.backend.payment.exception;

import java.io.Serial;

/**
 * Rzucany, gdy pakiet (ogłoszenia lub Boost) nie istnieje lub jest nieaktywny. Mapowany na HTTP 404
 * w GlobalExceptionHandler.
 */
public class PackageNotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public PackageNotFoundException() {
    super("Pakiet nie istnieje lub jest nieaktywny.");
  }
}
