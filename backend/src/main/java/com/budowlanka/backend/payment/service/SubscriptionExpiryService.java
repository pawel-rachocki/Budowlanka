package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logika wygaszania subskrypcji i ukrywania profili bez aktywnej subskrypcji. Wywoływana cyklicznie
 * przez {@link com.budowlanka.backend.payment.scheduler.SubscriptionExpiryScheduler}, ale
 * wydzielona jako transakcyjny serwis (scheduler pozostaje cienki, logika jest testowalna i
 * wywoływalna ręcznie).
 *
 * <p>Oba kroki to masowe, idempotentne {@code UPDATE} — ponowne uruchomienie na tym samym stanie
 * nie zmienia niczego (predykaty {@code active=true} / {@code visible=true} filtrują już
 * przetworzone rekordy).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryService {

  private final CrewSubscriptionRepository crewSubscriptionRepository;
  private final CrewProfileRepository crewProfileRepository;
  private final CrewBoostRepository crewBoostRepository;

  /** Wynik jednego przebiegu wygaszania. */
  public record ExpiryResult(int deactivatedSubscriptions, int hiddenProfiles) {}

  /**
   * Dezaktywuje wygasłe subskrypcje i ukrywa profile bez żadnej aktywnej subskrypcji. Krok
   * ukrywania liczy widoczność po {@code expires_at > now}, więc jest niezależny od kolejności
   * względem dezaktywacji.
   *
   * @return liczby zdezaktywowanych subskrypcji i ukrytych profili
   */
  @Transactional
  public ExpiryResult expireSubscriptions() {
    Instant now = Instant.now();

    int deactivated = crewSubscriptionRepository.deactivateExpired(now);
    int hidden = crewProfileRepository.hideProfilesWithoutActiveSubscription(now);

    long expiredBoosts = crewBoostRepository.countByExpiresAtBefore(now);
    if (expiredBoosts > 0) {
      log.debug(
          "Wygasłych boostów: {} (wygasają automatycznie przez @Formula, brak akcji)",
          expiredBoosts);
    }

    log.info("Wygaszanie subskrypcji: zdezaktywowano {}, ukryto profili {}", deactivated, hidden);
    return new ExpiryResult(deactivated, hidden);
  }
}
