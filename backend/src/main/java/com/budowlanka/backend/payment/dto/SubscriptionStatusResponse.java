package com.budowlanka.backend.payment.dto;

import java.time.Instant;

/**
 * Status subskrypcji i boosta ekipy dla dashboardu (GET /api/crew/subscription/me).
 *
 * <p>Brak aktywnej subskrypcji zwraca obiekt „pusty" ({@code hasActiveSubscription=false}, {@code
 * subscription=null}) zamiast 404 — front pokazuje wówczas CTA „Wykup pakiet".
 *
 * @param hasActiveSubscription czy ekipa ma aktywną subskrypcję (is_active i expires_at &gt; now)
 * @param isVisible aktualna flaga is_visible profilu ekipy
 * @param subscription aktywna subskrypcja lub {@code null}
 * @param boost aktywny boost lub {@code null}
 */
public record SubscriptionStatusResponse(
    boolean hasActiveSubscription,
    boolean isVisible,
    SubscriptionInfo subscription,
    BoostInfo boost) {

  /**
   * @param packageName nazwa pakietu ogłoszenia
   * @param expiresAt data wygaśnięcia subskrypcji
   * @param active czy subskrypcja jest aktywna (zawsze {@code true} — zwracamy tylko aktywne)
   */
  public record SubscriptionInfo(String packageName, Instant expiresAt, boolean active) {}

  /**
   * @param boostName nazwa pakietu Boost
   * @param expiresAt data wygaśnięcia boosta
   */
  public record BoostInfo(String boostName, Instant expiresAt) {}
}
