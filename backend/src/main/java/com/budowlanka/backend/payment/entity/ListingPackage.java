package com.budowlanka.backend.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Read-only katalog pakietów ogłoszeń. Rekordy seedowane przez Flyway (V015). */
@Entity
@Immutable
@Table(name = "listing_packages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ListingPackage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(name = "duration_days", nullable = false)
  private int durationDays;

  @Column(name = "price_pln", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePln;

  @Column(name = "is_active", nullable = false)
  private boolean active;
}
