package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.CrewSubscription;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewSubscriptionRepository extends JpaRepository<CrewSubscription, UUID> {

  // findFirst + ORDER BY ogranicza wynik do jednego wiersza — gdyby przejściowo istniały
  // dwie aktywne subskrypcje (wyścig webhooków), bierzemy najpóźniej wygasającą zamiast
  // rzucać NonUniqueResultException.
  Optional<CrewSubscription>
      findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
          UUID crewProfileId, Instant now);

  @Query("SELECT s FROM CrewSubscription s WHERE s.active = true AND s.expiresAt < :now")
  List<CrewSubscription> findExpired(@Param("now") Instant now);
}
