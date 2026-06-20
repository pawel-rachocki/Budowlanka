package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.CrewBoost;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewBoostRepository extends JpaRepository<CrewBoost, UUID> {

  Optional<CrewBoost> findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
      UUID crewProfileId, Instant now);
}
