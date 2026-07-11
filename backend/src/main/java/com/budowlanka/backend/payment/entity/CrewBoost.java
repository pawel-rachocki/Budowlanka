package com.budowlanka.backend.payment.entity;

import com.budowlanka.backend.crew.entity.CrewProfile;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "crew_boosts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrewBoost {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_profile_id", nullable = false, updatable = false)
  private CrewProfile crewProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "boost_package_id", nullable = false, updatable = false)
  private BoostPackage boostPackage;

  @Column(name = "starts_at", nullable = false, updatable = false)
  private Instant startsAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }

  /** Przedłuża boost do nowej daty wygaśnięcia (stack czasu — REM-164). */
  public void extendTo(Instant newExpiresAt) {
    this.expiresAt = newExpiresAt;
  }
}
