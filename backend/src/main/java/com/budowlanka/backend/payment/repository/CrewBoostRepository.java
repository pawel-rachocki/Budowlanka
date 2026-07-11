package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.CrewBoost;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewBoostRepository extends JpaRepository<CrewBoost, UUID> {

  Optional<CrewBoost> findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
      UUID crewProfileId, Instant now);

  /**
   * Liczba wygasłych boostów ({@code expires_at < now}). Boosty wygasają „same" przez
   * {@code @Formula hasActiveBoost} na profilu — ta metoda służy wyłącznie do logowania w
   * schedulerze, nie zmienia stanu.
   */
  long countByExpiresAtBefore(Instant now);
}
