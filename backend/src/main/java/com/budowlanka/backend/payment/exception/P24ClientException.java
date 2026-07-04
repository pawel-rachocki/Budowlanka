package com.budowlanka.backend.payment.exception;

/**
 * Błąd techniczny komunikacji z Przelewy24 (rejestracja/weryfikacja transakcji). Mapowanie na kod
 * HTTP (502/500) realizuje GlobalExceptionHandler w ramach B13.
 */
public class P24ClientException extends RuntimeException {

  public P24ClientException(String message) {
    super(message);
  }

  public P24ClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
