package com.budowlanka.backend.payment.exception;

import java.io.Serial;

/**
 * Rzucany, gdy podpis SHA384 notyfikacji webhooka Przelewy24 jest niezgodny z wyliczonym lokalnie.
 *
 * <p>Obsługiwany <strong>lokalnie</strong> w {@link
 * com.budowlanka.backend.payment.controller.WebhookController} (→ HTTP 400) — świadomie <strong>nie
 * jest</strong> mapowany w {@code GlobalExceptionHandler}. Dzięki temu P24 nigdy nie otrzyma 5xx z
 * powodu podpisu, a niezgodność (potencjalnie sfałszowane żądanie) jest odrzucana i logowana.
 */
public class InvalidWebhookSignatureException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public InvalidWebhookSignatureException(String sessionId) {
    super("Nieprawidłowy podpis notyfikacji Przelewy24 dla sessionId=" + sessionId);
  }
}
