package com.budowlanka.backend.payment.entity;

import com.budowlanka.backend.crew.entity.CrewProfile;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "crew_subscriptions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrewSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_profile_id", nullable = false, updatable = false)
  private CrewProfile crewProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "package_id", nullable = false, updatable = false)
  private ListingPackage listingPackage;

  @Column(name = "starts_at", nullable = false, updatable = false)
  private Instant startsAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }

  /** Przedłuża subskrypcję do nowej daty wygaśnięcia (logika „Przedłuż" — B8). */
  public void extendTo(Instant newExpiresAt) {
    this.expiresAt = newExpiresAt;
  }

  public void deactivate() {
    this.active = false;
  }
}
