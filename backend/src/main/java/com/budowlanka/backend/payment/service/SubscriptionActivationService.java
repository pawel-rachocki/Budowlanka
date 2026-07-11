package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.CrewBoost;
import com.budowlanka.backend.payment.entity.CrewSubscription;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.exception.PackageNotFoundException;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Aktywacja subskrypcji/boosta po zaksięgowaniu płatności — wołana z {@link PaymentWebhookService}
 * w tej samej transakcji, tuż przed oznaczeniem płatności jako COMPLETED. Idempotentność zapewnia
 * webhook (płatność {@code COMPLETED} nie trafia tu ponownie), dlatego tutaj skupiamy się na samej
 * aktywacji.
 *
 * <p>Przy inicjacji płatności {@code payments.reference_id} przechowuje identyfikator pakietu
 * (packageId). Tutaj odczytujemy z niego pakiet, aby poznać {@code durationDays}, a na koniec
 * przepinamy {@code reference_id} na utworzoną subskrypcję/boost (wymóg B8 — REM-146).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionActivationService {

  private final ListingPackageRepository listingPackageRepository;
  private final BoostPackageRepository boostPackageRepository;
  private final CrewSubscriptionRepository crewSubscriptionRepository;
  private final CrewBoostRepository crewBoostRepository;

  /**
   * Aktywuje uprawnienia wynikające z opłaconej płatności (LISTING → subskrypcja, BOOST → boost).
   *
   * @param payment płatność w stanie PENDING, potwierdzona przez P24 (verify=success)
   */
  public void activate(Payment payment) {
    switch (payment.getPaymentType()) {
      case LISTING -> activateListing(payment);
      case BOOST -> activateBoost(payment);
    }
  }

  /**
   * LISTING: przedłuża aktywną subskrypcję ({@code expiresAt = max(now, current) + durationDays})
   * albo — gdy jej brak — tworzy nową. Ustawia {@code is_visible=true}, o ile ekipa nie jest
   * zablokowana.
   */
  private void activateListing(Payment payment) {
    CrewProfile crew = payment.getCrewProfile();
    ListingPackage pkg =
        listingPackageRepository
            .findById(payment.getReferenceId())
            .orElseThrow(PackageNotFoundException::new);

    Instant now = Instant.now();
    Duration duration = Duration.ofDays(pkg.getDurationDays());

    UUID subscriptionId =
        crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                crew.getId(), now)
            .map(existing -> extendSubscription(existing, now, duration))
            .orElseGet(() -> createSubscription(crew, pkg, now, duration));

    if (!crew.isBlocked()) {
      crew.setVisible(true);
    }

    payment.linkActivatedResource(subscriptionId);
    log.info(
        "Aktywacja LISTING: płatność {} → subskrypcja {} dla ekipy {} (visible={})",
        payment.getId(),
        subscriptionId,
        crew.getId(),
        crew.isVisible());
  }

  private UUID extendSubscription(CrewSubscription existing, Instant now, Duration duration) {
    Instant base = existing.getExpiresAt().isAfter(now) ? existing.getExpiresAt() : now;
    existing.extendTo(base.plus(duration));
    return existing.getId();
  }

  private UUID createSubscription(
      CrewProfile crew, ListingPackage pkg, Instant now, Duration duration) {
    CrewSubscription subscription =
        crewSubscriptionRepository.save(
            CrewSubscription.builder()
                .crewProfile(crew)
                .listingPackage(pkg)
                .startsAt(now)
                .expiresAt(now.plus(duration))
                .active(true)
                .build());
    return subscription.getId();
  }

  /**
   * BOOST: przedłuża aktywny boost ({@code expiresAt = max(now, current) + durationDays}) albo —
   * gdy jego brak — tworzy nowy. Symetrycznie do {@link #activateListing} (REM-164) — kolejny zakup
   * stackuje czas zamiast tworzyć równoległe, nakładające się okna. Ranking podchwytuje aktywny
   * boost przez istniejące {@code @Formula hasActiveBoost} na {@link CrewProfile} — nie dotykamy tu
   * {@code is_visible}.
   */
  private void activateBoost(Payment payment) {
    CrewProfile crew = payment.getCrewProfile();
    BoostPackage pkg =
        boostPackageRepository
            .findById(payment.getReferenceId())
            .orElseThrow(PackageNotFoundException::new);

    Instant now = Instant.now();
    Duration duration = Duration.ofDays(pkg.getDurationDays());

    UUID boostId =
        crewBoostRepository
            .findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(crew.getId(), now)
            .map(existing -> extendBoost(existing, now, duration))
            .orElseGet(() -> createBoost(crew, pkg, now, duration));

    payment.linkActivatedResource(boostId);
    log.info(
        "Aktywacja BOOST: płatność {} → boost {} dla ekipy {}",
        payment.getId(),
        boostId,
        crew.getId());
  }

  private UUID extendBoost(CrewBoost existing, Instant now, Duration duration) {
    Instant base = existing.getExpiresAt().isAfter(now) ? existing.getExpiresAt() : now;
    existing.extendTo(base.plus(duration));
    return existing.getId();
  }

  private UUID createBoost(CrewProfile crew, BoostPackage pkg, Instant now, Duration duration) {
    CrewBoost boost =
        crewBoostRepository.save(
            CrewBoost.builder()
                .crewProfile(crew)
                .boostPackage(pkg)
                .startsAt(now)
                .expiresAt(now.plus(duration))
                .build());
    return boost.getId();
  }
}
