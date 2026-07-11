package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.payment.client.Przelewy24Client;
import com.budowlanka.backend.payment.dto.P24VerifyRequest;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Przetwarzanie notyfikacji Przelewy24 (webhook). Zakłada, że podpis został już zweryfikowany przez
 * {@link com.budowlanka.backend.payment.client.P24SignatureUtil} w controllerze — tutaj wyłącznie
 * logika biznesowa: idempotentność, potwierdzenie transakcji u P24 i aktywacja pakietu.
 *
 * <p>{@code sessionId} notyfikacji to nasze {@code payments.id} (ustawiane w {@link
 * PaymentService}), więc płatność znajdujemy przez {@code findById} — bez dodatkowej metody repo.
 *
 * <p>Metoda jest {@link Transactional}: dowolny wyjątek (np. {@code P24ClientException} z verify)
 * wycofuje zmiany, a wołający controller łapie go i zwraca 200, by P24 nie ponawiał w
 * nieskończoność przy błędach biznesowych.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookService {

  private final PaymentRepository paymentRepository;
  private final Przelewy24Client przelewy24Client;
  private final SubscriptionActivationService subscriptionActivationService;

  /** Przetwarza pojedynczą notyfikację P24 (po pomyślnej weryfikacji podpisu). */
  @Transactional
  public void process(P24WebhookNotification n) {
    Optional<Payment> maybePayment = findPayment(n.sessionId());
    if (maybePayment.isEmpty()) {
      log.warn("Webhook P24: brak płatności dla sessionId={} — pomijam", n.sessionId());
      return;
    }
    Payment payment = maybePayment.get();

    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      log.info("Webhook P24: płatność {} już COMPLETED — idempotentnie pomijam", payment.getId());
      return;
    }

    if (!amountMatches(payment, n)) {
      log.error(
          "Webhook P24: niezgodna kwota dla płatności {} (oczekiwano {} gr, otrzymano {} gr) — nie aktywuję",
          payment.getId(),
          toGrosze(payment),
          n.amount());
      return;
    }

    boolean confirmed =
        przelewy24Client.verifyTransaction(
            new P24VerifyRequest(n.sessionId(), n.amount(), n.currency(), n.orderId()));
    if (!confirmed) {
      log.warn(
          "Webhook P24: verify nie potwierdził płatności {} (orderId={}) — oznaczam FAILED",
          payment.getId(),
          n.orderId());
      payment.markFailed();
      return;
    }

    subscriptionActivationService.activate(payment);
    payment.markCompleted(String.valueOf(n.orderId()));
    log.info(
        "Webhook P24: płatność {} zaksięgowana (orderId={}), pakiet aktywowany",
        payment.getId(),
        n.orderId());
  }

  private Optional<Payment> findPayment(String sessionId) {
    UUID id;
    try {
      id = UUID.fromString(sessionId);
    } catch (IllegalArgumentException e) {
      log.warn("Webhook P24: sessionId '{}' nie jest poprawnym UUID", sessionId);
      return Optional.empty();
    }
    return paymentRepository.findById(id);
  }

  private static boolean amountMatches(Payment payment, P24WebhookNotification n) {
    return toGrosze(payment) == n.amount();
  }

  /** Kwota płatności w groszach (int) — do porównania z {@code amount} z notyfikacji P24. */
  private static int toGrosze(Payment payment) {
    return payment.getAmountPln().movePointRight(2).intValueExact();
  }
}
