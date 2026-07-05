package com.budowlanka.backend.payment.exception;

/**
 * Błąd techniczny komunikacji z Przelewy24 (rejestracja/weryfikacja transakcji). Mapowanie na HTTP
 * 502 realizuje {@code GlobalExceptionHandler} (B13).
 *
 * <p>To jest wyjątek określany w ticketcie REM-151 jako {@code PaymentInitiationException} — nazwa
 * świadomie odbiega od ticketu, bo klasa powstała wcześniej (B5) i jest już rzucana z {@link
 * com.budowlanka.backend.payment.client.Przelewy24Client} zarówno przy register, jak i verify.
 */
public class P24ClientException extends RuntimeException {

  public P24ClientException(String message) {
    super(message);
  }

  public P24ClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
