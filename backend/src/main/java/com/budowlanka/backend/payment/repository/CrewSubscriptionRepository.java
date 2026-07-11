package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.CrewSubscription;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewSubscriptionRepository extends JpaRepository<CrewSubscription, UUID> {

  // findFirst + ORDER BY ogranicza wynik do jednego wiersza — gdyby przejściowo istniały
  // dwie aktywne subskrypcje (wyścig webhooków), bierzemy najpóźniej wygasającą zamiast
  // rzucać NonUniqueResultException.
  Optional<CrewSubscription>
      findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
          UUID crewProfileId, Instant now);

  /**
   * Czy ekipa ma wciąż aktywną subskrypcję ({@code is_active=true} i {@code expires_at > now}).
   * Używane do przeliczenia widoczności profilu przy odblokowaniu przez admina — profil odsłaniamy
   * tylko wtedy, gdy jego opłacona subskrypcja nadal obowiązuje.
   */
  boolean existsByCrewProfileIdAndActiveTrueAndExpiresAtAfter(UUID crewProfileId, Instant now);

  /**
   * Masowo dezaktywuje subskrypcje, które wygasły ({@code expires_at < now}) a wciąż mają flagę
   * {@code is_active=true}. Idempotentne — predykat {@code active=true} pomija już wygaszone.
   * {@code clearAutomatically} czyści kontekst persystencji po bulk-update.
   *
   * @return liczba zdezaktywowanych subskrypcji
   */
  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE CrewSubscription s SET s.active = false "
          + "WHERE s.active = true AND s.expiresAt < :now")
  int deactivateExpired(@Param("now") Instant now);
}
