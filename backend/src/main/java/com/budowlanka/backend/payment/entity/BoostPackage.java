package com.budowlanka.backend.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Read-only katalog pakietów Boost. Rekordy seedowane przez Flyway (V015). */
@Entity
@Immutable
@Table(name = "boost_packages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoostPackage {

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
