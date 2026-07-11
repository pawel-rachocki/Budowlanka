package com.budowlanka.backend.payment.scheduler;

import com.budowlanka.backend.payment.service.SubscriptionExpiryService;
import com.budowlanka.backend.payment.service.SubscriptionExpiryService.ExpiryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cykliczny job wygaszania subskrypcji i ukrywania profili bez aktywnej subskrypcji. Cienka warstwa
 * — cała logika w {@link SubscriptionExpiryService}. Domyślnie co godzinę; cron konfigurowalny
 * przez {@code app.scheduling.subscription-expiry-cron}, strefa {@code Europe/Warsaw} (niezależnie
 * od strefy JVM na prodzie).
 *
 * <p>Wyjątki są łapane i logowane — Spring domyślnie połyka je bez stacktrace'u, a pominięcie
 * jednego przebiegu jest akceptowalne (kolejny za godzinę, logika idempotentna).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryScheduler {

  private final SubscriptionExpiryService subscriptionExpiryService;

  @Scheduled(
      cron = "${app.scheduling.subscription-expiry-cron:0 0 * * * *}",
      zone = "Europe/Warsaw")
  public void run() {
    try {
      ExpiryResult result = subscriptionExpiryService.expireSubscriptions();
      log.debug(
          "Job wygaszania subskrypcji zakończony: {} dezaktywacji, {} ukryć",
          result.deactivatedSubscriptions(),
          result.hiddenProfiles());
    } catch (Exception e) {
      log.error(
          "Job wygaszania subskrypcji zakończony błędem — kolejna próba w następnym cyklu", e);
    }
  }
}
