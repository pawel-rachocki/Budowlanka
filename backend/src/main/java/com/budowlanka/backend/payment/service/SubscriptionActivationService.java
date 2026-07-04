package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Aktywacja subskrypcji/boosta po zaksięgowaniu płatności — wołana z {@link PaymentWebhookService}
 * w tej samej transakcji, tuż przed oznaczeniem płatności jako COMPLETED.
 *
 * <p><strong>Placeholder dla B8 (REM-146).</strong> Pełna logika (utworzenie {@code
 * crew_subscriptions}/{@code crew_boosts}, przedłużanie aktywnej subskrypcji, ustawienie {@code
 * crew_profiles.is_visible=true} gdy {@code blocked=false}) wchodzi z REM-146. Na razie tylko
 * loguje — flow webhooka jest kompletny i testowalny end-to-end, a payment poprawnie przechodzi w
 * COMPLETED.
 */
@Service
@Slf4j
public class SubscriptionActivationService {

  /**
   * Aktywuje uprawnienia wynikające z opłaconej płatności (LISTING → subskrypcja, BOOST → boost).
   *
   * @param payment płatność w stanie PENDING, potwierdzona przez P24 (verify=success)
   */
  public void activate(Payment payment) {
    // TODO(REM-146 / B8): utworzenie/przedłużenie crew_subscriptions lub crew_boosts + is_visible.
    log.info(
        "Aktywacja pakietu dla płatności {} typu {} (referenceId={}) — placeholder B8/REM-146",
        payment.getId(),
        payment.getPaymentType(),
        payment.getReferenceId());
  }
}
