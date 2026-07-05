package com.budowlanka.backend.payment.controller;

import com.budowlanka.backend.payment.client.P24SignatureUtil;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import com.budowlanka.backend.payment.exception.InvalidWebhookSignatureException;
import com.budowlanka.backend.payment.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publiczny endpoint notyfikacji Przelewy24 (webhook na {@code urlStatus}).
 *
 * <p>Kolejność: (1) weryfikacja podpisu — niezgodny podpis to potencjalnie sfałszowane żądanie,
 * odrzucamy przez 400 (legalny ruch z P24 zawsze ma poprawny podpis; 400 ujawnia błędną
 * konfigurację CRC podczas testów sandbox). (2) po przejściu podpisu delegujemy do {@link
 * PaymentWebhookService} w {@code try/catch} — <strong>zawsze 200</strong>, by P24 nie ponawiał w
 * nieskończoność przy błędach biznesowych (błędy są logowane).
 *
 * <p>Endpoint jest {@code permitAll} w {@link com.budowlanka.backend.config.SecurityConfig} —
 * autentykacja opiera się wyłącznie na podpisie P24, nie na JWT.
 */
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

  private final P24SignatureUtil signatureUtil;
  private final PaymentWebhookService webhookService;

  @PostMapping("/p24")
  public ResponseEntity<Void> handleP24(@RequestBody P24WebhookNotification notification) {
    try {
      if (!signatureUtil.verifyWebhookSignature(notification)) {
        throw new InvalidWebhookSignatureException(notification.sessionId());
      }
      webhookService.process(notification);
    } catch (InvalidWebhookSignatureException e) {
      // Niezgodny podpis (potencjalnie sfałszowane żądanie) — odrzucamy przez 400, bez akcji.
      log.warn("Webhook P24: {} — odrzucam", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      // Zawsze 200 po przejściu podpisu — inaczej P24 ponawia notyfikację w nieskończoność.
      log.error(
          "Webhook P24: błąd przetwarzania notyfikacji dla sessionId={}: {}",
          notification.sessionId(),
          e.getMessage(),
          e);
    }
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
